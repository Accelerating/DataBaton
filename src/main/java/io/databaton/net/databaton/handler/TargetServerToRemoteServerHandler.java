package io.databaton.net.databaton.handler;

import com.google.protobuf.ByteString;
import io.databaton.config.DataBatonConfig;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonMessage;
import io.databaton.utils.RunUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetServerToRemoteServerHandler extends ChannelInboundHandlerAdapter {

    private final Channel toLocalServerChannel;

    private final DataBatonConfig dataBatonConfig;

    public TargetServerToRemoteServerHandler(Channel toLocalServerChannel, DataBatonConfig dataBatonConfig) {
        this.toLocalServerChannel = toLocalServerChannel;
        this.dataBatonConfig = dataBatonConfig;
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
            RunUtils.runIfSatisfy(dataBatonConfig.getDebug(), ()->{
                log.info("dispatch data to remote server, host:{}", builder.getDstHost());
            });
            toLocalServerChannel.writeAndFlush(new DataBatonMessage(OpType.DISPATCH.genOperationTypeBytes(), payload));
        }else{
            toLocalServerChannel.close();
            ReferenceCountUtil.release(msg);
        }
    }


}
