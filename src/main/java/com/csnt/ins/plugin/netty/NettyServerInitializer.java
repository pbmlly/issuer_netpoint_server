package com.csnt.ins.plugin.netty;

import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.factory.ContextSSLFactory;
import com.csnt.ins.utils.SysConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * @author source
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyEncoder encoder =new NettyEncoder();
    //生产上 这里配置的是200个线程
    private final EventExecutorGroup EVENT_EXEC = new DefaultEventExecutorGroup(SysConfig.CONFIG.getInt("netty.nThreads", 3));

    private Consumer<DataInfo> consumer;
    private SslHandler sslHandler;

    public NettyServerInitializer(Consumer<DataInfo> consumer) {
        super();
        this.consumer = consumer;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //压缩和解压缩;

        //用于处理大数据流,比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的,增加ChunkedWriteHandler 这个handler我们就不用考虑这个问题了
        pipeline.addLast("ChunkedWriteHandler",new ChunkedWriteHandler())
                .addLast("encoder",encoder)
                .addLast("IdleStateHandler",new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                .addLast("decoder",new NettyDecoder())
                .addLast(EVENT_EXEC, new NettyServerHandler(consumer));
//                .addFirst(getSslHandler());
        SSLEngine engine = ContextSSLFactory.getSslContext().createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);
        pipeline.addFirst("ssl", new SslHandler(engine));
    }

    private SslHandler getSslHandler(){
        if(sslHandler == null ){
            SSLEngine sslEngine = ContextSSLFactory.getSslContext().createSSLEngine() ;
            sslEngine.setUseClientMode(false) ;
            //false为单向认证，true为双向认证
            sslEngine.setNeedClientAuth(true) ;
            sslHandler = new SslHandler(sslEngine);
        }
        return sslHandler ;
    }
}
