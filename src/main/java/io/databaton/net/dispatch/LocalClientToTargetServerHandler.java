package io.databaton.net.dispatch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class LocalClientToTargetServerHandler extends ChannelInboundHandlerAdapter {

    private final Channel toTargetServerChannel;

    public LocalClientToTargetServerHandler(Channel toTargetServerChannel){
        this.toTargetServerChannel = toTargetServerChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if(toTargetServerChannel.isActive()){
            toTargetServerChannel.writeAndFlush(msg);
        }else{
            ReferenceCountUtil.release(msg);
        }
    }

}
