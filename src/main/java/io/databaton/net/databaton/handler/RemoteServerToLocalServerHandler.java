package io.databaton.net.databaton.handler;

import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteServerToLocalServerHandler extends SimpleChannelInboundHandler<DataBatonDispatchMessageProto.DataBatonDispatchMessage> {

    private Channel toLocalClientChannel;

    public RemoteServerToLocalServerHandler(Channel toLocalClientChannel){
        this.toLocalClientChannel = toLocalClientChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception {
//        log.info("proxy server return data, target server:{}", msg.getDstHost());
        if(toLocalClientChannel.isActive()){
            byte[] data = msg.getData().toByteArray();
            ByteBuf buf = ctx.alloc().buffer(data.length);
            buf.writeBytes(data);
            toLocalClientChannel.writeAndFlush(buf);
        }
    }
}