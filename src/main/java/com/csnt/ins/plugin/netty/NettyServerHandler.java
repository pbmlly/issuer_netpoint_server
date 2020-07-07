package com.csnt.ins.plugin.netty;

import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.jfinal.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.function.Consumer;


/**
 * @author source
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<DataInfo> {

    private static Log logger = Log.getLog("netty");

    Consumer<DataInfo> consumer;

    public NettyServerHandler(Consumer<DataInfo> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        super.channelActive(ctx);

        logger.debug("connect sucessfull!!!!!!!!!!!!!!!!!...." + insocket.getAddress() + ":" + insocket.getPort());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        super.channelInactive(ctx);

        logger.debug("close sucessfull!!!!!!!!!!!!!!!!!!...." + insocket.getAddress() + ":" + insocket.getPort());
    }

    /**
     * 顺序遍历所有pipeline, 传入数据, 获取返回数据, 返回返回数据到业务端,因writeAndFlush接口异步,
     * 所以必须监听所有写请求处理完,再关闭连接
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataInfo dataInfo) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        logger.debug("channelRead0 successfull!...." + insocket.getAddress());

        ChannelFuture channelFuture = null;
        try {
            if (channelHandlerContext.channel().isActive()) {
                consumer.accept(dataInfo);
                logger.debug("writeAndFlush......." + insocket.getAddress());
                channelFuture = channelHandlerContext.writeAndFlush(dataInfo);

                if (dataInfo.isError()) {
                    channelHandlerContext.channel().close();
                    channelHandlerContext.close();
                    logger.debug("client!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + dataInfo.getClientIp() + " close connection!!");
                }


            } else {
                channelHandlerContext.channel().close();
                channelHandlerContext.close();
                logger.debug("client!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + dataInfo.getClientIp() + " close connection!!");
            }
        } catch (Throwable t) {
            String responseJson = "{\"msg\": \"业务处理失败 %s\",\"code\": %d,\"data\": null,\"success\": false}";
            String errorMsg = String.format(responseJson, t.getMessage(), ResponseStatusEnum.SYS_INVALID_PARAM.getCode());
            logger.error(dataInfo.getClientIp() + ":" + dataInfo.getClientPort() + "->" + errorMsg);
            dataInfo.setResponseJson(errorMsg);
            dataInfo.setError(true);
            channelFuture = channelHandlerContext.writeAndFlush(dataInfo);
            logger.error("netty handle error", t);
        } finally {
            if (channelFuture != null) {
                channelFuture.addListener(future -> {
                    ReferenceCountUtil.release(dataInfo);
                    // channelHandlerContext.close();
                    logger.debug("write response data sucessfull!...." + insocket.getAddress());
                });
            } else {
                ReferenceCountUtil.release(dataInfo);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught! ", cause);
        Channel channel = ctx.channel();
        if (!channel.isActive()) {
            logger.debug("client!!!!!!!!!!!!!!!!!!!!!" + channel.remoteAddress() + " close connection!!");
            ctx.channel().close();
            channel.close();
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }
}
