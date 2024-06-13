package io.databaton.net.databaton.tcp.handler;

import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * remote(proxy) server --->  local server
 * @author zxx
 */
@Slf4j
public class RemoteServerToLocalServerTcpHandler extends SimpleChannelInboundHandler<DataBatonDispatchMessageProto.DataBatonDispatchMessage> {

    private final Channel toLocalClientChannel;
    private final DataBatonContext dataBatonContext;

    public RemoteServerToLocalServerTcpHandler(Channel toLocalClientChannel, DataBatonContext dataBatonContext){
        this.toLocalClientChannel = toLocalClientChannel;
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception {
        log.debug("proxy server return data, target server:{}", msg.getDstHost());
        if(toLocalClientChannel.isActive()){
            byte[] data = msg.getData().toByteArray();
            ByteBuf buf = ctx.alloc().buffer(data.length);

            buf.writeBytes(data);
            toLocalClientChannel.writeAndFlush(buf);


        }
    }
}