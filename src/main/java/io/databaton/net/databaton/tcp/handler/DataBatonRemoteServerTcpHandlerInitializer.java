package io.databaton.net.databaton.tcp.handler;

import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.tcp.codec.DataBatonTcpDecryptDecoder;
import io.databaton.net.databaton.tcp.codec.DataBatonTcpEncryptEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;


/**
 * @author zxx
 */
public class DataBatonRemoteServerTcpHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private final DataBatonContext dataBatonContext;

    public DataBatonRemoteServerTcpHandlerInitializer(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DataBatonTcpEncryptEncoder(dataBatonContext));
        pipeline.addLast(new DataBatonTcpDecryptDecoder(dataBatonContext));
        pipeline.addLast(new DataBatonAuthenticationTcpHandler(dataBatonContext));
        pipeline.addLast(new DataBatonDispatchTcpHandler(dataBatonContext));
    }
}
