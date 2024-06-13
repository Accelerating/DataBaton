package io.databaton.net.databaton.udp.handler;

import io.databaton.net.databaton.DataBatonContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

/**
 * @author zxx
 */
public class DataBatonRemoteUdpServerHandlerInitializer extends ChannelInitializer<DatagramChannel> {

    private DataBatonContext dataBatonContext;

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast();
    }
}
