package com.csnt.ins.plugin.netty;

import com.csnt.ins.bean.netty.DataInfo;
import com.jfinal.log.Log;
import com.jfinal.plugin.IPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.function.Consumer;


/**
 * @author source
 */
public class NettyServerPlugin implements IPlugin {
    private static Log LOG = Log.getLog("netty");
    private int port;
    private final static int BACKLOG = 128;
    private Consumer<DataInfo> consumer;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServerPlugin(int port, Consumer<DataInfo> consumer) {
        this.port = port;
        this.consumer = consumer;
    }

    @Override
    public boolean start() {
        // 服务器绑定端口监听, 监听服务器关闭监听
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new NettyServerInitializer(consumer))
                    //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    //Option是为了NioServerSocketChannel设置的，用来接收传入连接的
                    .option(ChannelOption.SO_BACKLOG, BACKLOG)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(4096))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture cf = b.bind(port).sync();
            //阻塞, 多线程时，异步阻塞，关闭
//            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            return false;
        }
        LOG.info("NettyServer start successfull!");
        return true;
    }


    @Override
    public boolean stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        LOG.info("NettyServer stop successfull!");
        return true;
    }

}
