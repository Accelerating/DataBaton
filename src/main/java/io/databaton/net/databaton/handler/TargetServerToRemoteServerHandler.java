package io.databaton.net.databaton.handler;

import com.google.protobuf.ByteString;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetServerToRemoteServerHandler extends ChannelInboundHandlerAdapter {

    private Channel toLocalServerChannel;

    public TargetServerToRemoteServerHandler(Channel toLocalServerChannel) {
        this.toLocalServerChannel = toLocalServerChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(toLocalServerChannel.isActive()){
            ByteBuf buf = (ByteBuf) msg;
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
            builder.setData(ByteString.copyFrom(data));
            builder.setDstHost(ctx.channel().remoteAddress().toString());
            builder.setDstPort(0);

            byte[] payload = builder.build().toByteArray();
//            log.info("target server return data, target server:{}", builder.getDstHost());
            toLocalServerChannel.writeAndFlush(new DataBatonMessage(OpType.DISPATCH.genOperationTypeBytes(), payload));
        }else{
            toLocalServerChannel.close();
            ReferenceCountUtil.release(msg);
        }
    }


}
