package io.databaton.server;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.codec.DataBatonEncryptEncoder;
import io.databaton.net.databaton.handler.DataBatonAuthenticationHandler;
import io.databaton.net.databaton.handler.DataBatonDispatchHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

public class DataBatonRemoteServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private NioEventLoopGroup clientGroup;
    private DataBatonConfig dataBatonConfig;
    private CryptProcessor cryptProcessor;

    public DataBatonRemoteServerHandlerInitializer(NioEventLoopGroup clientGroup, DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor) {
        this.clientGroup = clientGroup;
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DataBatonEncryptEncoder(cryptProcessor, dataBatonConfig));
        pipeline.addLast(new DataBatonDecryptDecoder(cryptProcessor, dataBatonConfig));
        pipeline.addLast(new DataBatonAuthenticationHandler(dataBatonConfig));
        pipeline.addLast(new DataBatonDispatchHandler(clientGroup, dataBatonConfig));
    }
}
