package io.databaton.server;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.socks5.Socks5InitialRequestHandler;
import io.databaton.net.socks5.Socks5CommandRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;

public class DataBatonLocalServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private final NioEventLoopGroup clientGroup;
    private final DataBatonConfig dataBatonConfig;
    private final CryptProcessor cryptProcessor;

    public DataBatonLocalServerHandlerInitializer(NioEventLoopGroup clientGroup, DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor){
        this.clientGroup = clientGroup;
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(Socks5ServerEncoder.DEFAULT);

        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(new Socks5InitialRequestHandler());

        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(new Socks5CommandRequestHandler(clientGroup, dataBatonConfig, cryptProcessor));
    }

}
