package io.databaton.net.dispatch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class TargetServerToLocalClientHandler extends ChannelInboundHandlerAdapter {

    private final Channel targetServerToLocalClientChannel;

    public TargetServerToLocalClientHandler(Channel targetServerToLocalClientChannel){
        this.targetServerToLocalClientChannel = targetServerToLocalClientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (targetServerToLocalClientChannel.isActive()) {
            targetServerToLocalClientChannel.writeAndFlush(msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

}
