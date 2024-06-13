package io.databaton.net.databaton.tcp.handler;

import com.google.protobuf.ByteString;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonMessage;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * target server ---> remote(proxy) server
 * @author zxx
 */
@Slf4j
public class TargetServerToRemoteServerTcpHandler extends ChannelInboundHandlerAdapter {

    private final Channel toLocalServerChannel;

    private final DataBatonContext dataBatonContext;

    public TargetServerToRemoteServerTcpHandler(Channel toLocalServerChannel, DataBatonContext dataBatonContext) {
        this.toLocalServerChannel = toLocalServerChannel;
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            if(toLocalServerChannel.isActive()){
                ByteBuf buf = (ByteBuf) msg;
                byte[] data = new byte[buf.readableBytes()];

                buf.readBytes(data);
                DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
                builder.setData(ByteString.copyFrom(data));
                builder.setDstHost(ctx.channel().remoteAddress().toString());
                builder.setDstPort(0);

                byte[] payload = builder.build().toByteArray();
                log.debug("dispatch data to remote server, host:{}", builder.getDstHost());
                toLocalServerChannel.writeAndFlush(new DataBatonMessage(OpType.DISPATCH.genOperationTypeBytes(), payload));
            }else{
                toLocalServerChannel.close();
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }


}
