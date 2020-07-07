package com.csnt.ins.plugin.netty;

import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.NettyUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Arrays;
import java.util.List;

/**
 * Created by huangxiang on 2017/5/24.mei
 * 解码器
 */
public class NettyDecoder extends ByteToMessageDecoder {
    private static Log logger = Log.getLog("netty");
    private static final int MAJIC_NUMBER = 0;
    private static final int HEADER_BIG_LENGTH = 8;
    private final static int MAX_UN_REC_PING_TIMES = 2;


    private volatile int unRecPingTimes = 0;
    private static int limitRead = 1048576;

    private boolean isReadHead = true;
    private int dataLength = 0;
    private int curLength = 0;
    private DataInfo nettyDataInfo = null;
    private byte[] outData = null;
    private AttributeKey<String> attributeIpKey = AttributeKey.valueOf("RealIp");
    private AttributeKey<Integer> attributePortKey = AttributeKey.valueOf("RealHost");


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        logger.debug("netty begin decoder data in buf length :"+in.readableBytes());
        unRecPingTimes = 0;
        if (isReadHead) {
            if (!decodeHead(ctx, in, out)) {
                return;
            }
        }

        if (!isReadHead && dataLength > 0 && curLength < dataLength && outData != null) {
            decodeData(ctx, in, out);
        } else {
            logger.error("netty网络层拆包异常:isReadHead->" + isReadHead + ",dataLength->" + dataLength + ",curLength->" + curLength + ",outData->" + Arrays.toString(outData));
        }

    }

    private void decodeData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int readLen = in.readableBytes();

        if (readLen + curLength > dataLength) {
            readLen = dataLength - curLength;
        }

        decodeSmallData(in, readLen);

        if (curLength >= dataLength) {
            if (outData != null) {
                String request = new String(outData, StringUtil.UTF8);
                String[] splits = request.split(StringUtil.RECEIVE_MSG_SPLIT);
                //0x00-msgLen-msgType;;userId;;authorization;;msgMd5;;jsonObj ({…}|[…])
                if (splits.length == 5) {
                    nettyDataInfo.setMsgType(splits[0]);
                    nettyDataInfo.setUserId(splits[1]);
                    nettyDataInfo.setAuthorization(splits[2]);
                    nettyDataInfo.setRequestJsonMd5(splits[3]);
                    nettyDataInfo.setRequestJson(splits[4]);
                    //传递正常解析消息
                    out.add(nettyDataInfo);
                }
            }
            reset();
        }
    }

    private void decodeSmallData(ByteBuf in, int readLen) {
        in.readBytes(outData, curLength, readLen);
        curLength += readLen;
    }


    private void reset() {
        isReadHead = true;
        dataLength = 0;
        curLength = 0;
        outData = null;
        nettyDataInfo = null;
    }


    private boolean decodeHead(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < HEADER_BIG_LENGTH) {
            return false;
        }

        //获取客户端ip,端口，服务器ip
        String clientIp = getClentIp(ctx);
        int clientPort = getClentPort(ctx);

        //构建元数据
        nettyDataInfo = new DataInfo(clientIp, clientPort);

        //标记读index,读取数据
        in.markReaderIndex();

        /*魔数相当于预留4个字节0x0*/
        int magicNumber = in.readInt();

        //读取数据长度
        this.dataLength = in.readInt();

        //消息头有问题的处理
        if (magicNumber != MAJIC_NUMBER || this.dataLength <= 0 || this.dataLength > limitRead) {
            //取出本次接收所有数据存入元数据IDataInfo中，封装异常信息到元数据IDataInfo中，直接本地化存储最后，不走后续业务流程了
            String errorMsg = String.format("nmagicNumber: %d, length: %d", magicNumber, dataLength);
            errorMsg = String.format("{\"msg\": \"数据格式异常，消息体解析失败: %s\",\"code\": %d,\"data\": null,\"success\": false}", errorMsg, ResponseStatusEnum.SYS_INVALID_PARAM.getCode());

            //重置读数据流， 读出所有数据
            in.markReaderIndex();
            int readLen = in.readableBytes();
            byte[] readBytes = new byte[readLen];
            in.readBytes(readBytes);
            logger.debug("netty errorbody....." + errorMsg);
            nettyDataInfo.setResponseJson(errorMsg);
            nettyDataInfo.setError(true);

            //返回客户端异常
            out.add(nettyDataInfo);

            reset();
            return false;
        }

        isReadHead = false;
        curLength = 0;

        //内容较小，直接内存
        outData = new byte[dataLength];

        logger.debug("ip:" + clientIp + ", port: " + clientPort + "  decode head success : rcv date len " + dataLength);
        return true;
    }

    private int getClentPort(ChannelHandlerContext ctx) {
        Attribute<Integer> attributePort = ctx.channel().attr(attributePortKey);
        if (attributePort != null && attributePort.get() != null) {
            return attributePort.get();
        } else {
            return NettyUtil.getClientPort(ctx);
        }
    }

    private String getClentIp(ChannelHandlerContext ctx) {
        Attribute<String> attributeIp = ctx.channel().attr(attributeIpKey);
        if (attributeIp != null && attributeIp.get() != null) {
            return attributeIp.get();
        } else {
            return NettyUtil.getClientIp(ctx);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                // 失败计数器次数大于等于2次的时候，关闭链接，等待client重连
                if (unRecPingTimes >= MAX_UN_REC_PING_TIMES) {
                    // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                    ctx.close();
                    logger.info("idle!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +
                            "!!! close connect!!!　" + ctx.channel().remoteAddress());
                } else {
                    // 失败计数器加1
                    unRecPingTimes++;
                    logger.debug("idle!!!!!!!!" + unRecPingTimes);
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
