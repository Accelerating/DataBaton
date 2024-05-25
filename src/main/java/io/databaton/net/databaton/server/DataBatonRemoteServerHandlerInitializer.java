package io.databaton.net.databaton.server;

import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.tcp.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.tcp.codec.DataBatonEncryptEncoder;
import io.databaton.net.databaton.tcp.handler.DataBatonAuthenticationHandler;
import io.databaton.net.databaton.tcp.handler.DataBatonDispatchHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class DataBatonRemoteServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private final DataBatonContext dataBatonContext;

    public DataBatonRemoteServerHandlerInitializer(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DataBatonEncryptEncoder(dataBatonContext));
        pipeline.addLast(new DataBatonDecryptDecoder(dataBatonContext));
        pipeline.addLast(new DataBatonAuthenticationHandler(dataBatonContext));
        pipeline.addLast(new DataBatonDispatchHandler(dataBatonContext));
    }
}
