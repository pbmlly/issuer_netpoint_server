package com.csnt.ins.plugin.netty;


import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author source
 */
@Sharable
public class NettyEncoder extends MessageToByteEncoder<DataInfo> {
    private static Log logger = Log.getLog("netty");

    @Override
    protected void encode(ChannelHandlerContext ctx, DataInfo msg, ByteBuf out) {
        try {
            encodeFromMemory(ctx, msg, out);
        } catch (Throwable e) {
            logger.error("netty NettyEncoder error：" + e.getMessage(), e);
        }

    }

    private void encodeFromMemory(ChannelHandlerContext ctx, DataInfo nettyDataInfo, ByteBuf out) {
        //0x00-msgLen-msgType;;userId;;msgMd5;;authorization;;jsonObj ({…}|[…])
        String responseJson = nettyDataInfo.getResponseJson();
        String response = nettyDataInfo.getMsgType() + StringUtil.RECEIVE_MSG_SPLIT +
                nettyDataInfo.getUserId() + StringUtil.RECEIVE_MSG_SPLIT +
                nettyDataInfo.getAuthorization() + StringUtil.RECEIVE_MSG_SPLIT +
                UtilMd5.EncoderByMd5(responseJson) + StringUtil.RECEIVE_MSG_SPLIT +
                responseJson;
        byte[] responseBytes = response.getBytes(StringUtil.UTF8);
        out.writeInt(0x00);
        out.writeInt(responseBytes.length);
        out.writeBytes(responseBytes);
        logger.debug("netty NettyEncoder encode msg : " + nettyDataInfo.getResponseJson() + "," + nettyDataInfo.getClientIp());
    }


}