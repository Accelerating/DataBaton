package io.databaton.net.dispatch;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


/**
 * client(e.g., browser)  --->  target host
 * @author zxx
 */
public class LocalClientToTargetServerHandler extends ChannelInboundHandlerAdapter {

    private final Channel toTargetServerChannel;

    public LocalClientToTargetServerHandler(Channel toTargetServerChannel){
        this.toTargetServerChannel = toTargetServerChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(toTargetServerChannel.isActive()){
            toTargetServerChannel.writeAndFlush(msg);
        }else{
            ReferenceCountUtil.release(msg);
        }
    }

}
