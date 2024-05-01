package io.databaton.net.databaton.handler;

import io.databaton.config.DataBatonConfig;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteServerToLocalServerHandler extends SimpleChannelInboundHandler<DataBatonDispatchMessageProto.DataBatonDispatchMessage> {

    private final Channel toLocalClientChannel;
    private final DataBatonConfig dataBatonConfig;

    public RemoteServerToLocalServerHandler(Channel toLocalClientChannel, DataBatonConfig dataBatonConfig){
        this.toLocalClientChannel = toLocalClientChannel;
        this.dataBatonConfig = dataBatonConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception {
        if(dataBatonConfig.getDebug()) {
            log.info("proxy server return data, target server:{}", msg.getDstHost());
        };

        if(toLocalClientChannel.isActive()){
            byte[] data = msg.getData().toByteArray();
            ByteBuf buf = ctx.alloc().buffer(data.length);
            try{
                buf.writeBytes(data);
                toLocalClientChannel.writeAndFlush(buf);
            }finally {
//                if(buf.refCnt() > 0){
//                    buf.release();
//                }
            }

        }
    }
}