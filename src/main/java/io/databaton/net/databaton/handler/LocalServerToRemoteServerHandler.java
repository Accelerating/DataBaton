package io.databaton.net.databaton.handler;

import com.google.protobuf.ByteString;
import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonServerConfig;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.model.DataBatonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalServerToRemoteServerHandler extends ChannelInboundHandlerAdapter {

    private static final int STATE_INIT = 0;
    private static final int STATE_CONNECTED = 1;
    private int status = STATE_INIT;

    private final DataBatonConfig dataBatonConfig;
    private final Channel toRemoteServerChannel;
    private final String dstHost;
    private final int dstPort;

    public LocalServerToRemoteServerHandler(Channel toRemoteServerChannel, String dstHost, int dstPort, DataBatonConfig dataBatonConfig){
        this.toRemoteServerChannel = toRemoteServerChannel;
        this.dstHost = dstHost;
        this.dstPort = dstPort;
        this.dataBatonConfig = dataBatonConfig;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try{
            if(toRemoteServerChannel.isActive()){
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                if(status == STATE_INIT){
                    DataBatonServerConfig remoteServer = dataBatonConfig.getRemoteServer();
                    DataBatonLoginMessageProto.DataBatonLoginMessage.Builder builder = DataBatonLoginMessageProto.DataBatonLoginMessage.newBuilder();
                    builder.setUsername(remoteServer.getUsername());
                    builder.setPassword(remoteServer.getPassword());
                    builder.setDstHost(dstHost);
                    builder.setDstPort(dstPort);
                    builder.setData(ByteString.copyFrom(data));
                    status = STATE_CONNECTED;
                    byte[] payload = builder.build().toByteArray();
                    toRemoteServerChannel.writeAndFlush(new DataBatonMessage(OpType.LOGIN.genOperationTypeBytes(), payload));
                    log.debug("auth to remote server, host:{}, port:{}", dstHost, dstPort);
                }else if(status == STATE_CONNECTED){
                    DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
                    builder.setDstHost(dstHost);
                    builder.setDstPort(dstPort);
                    builder.setData(ByteString.copyFrom(data));
                    byte[] payload = builder.build().toByteArray();
                    toRemoteServerChannel.writeAndFlush(new DataBatonMessage(OpType.DISPATCH.genOperationTypeBytes(), payload));

                    log.debug("dispatch data to remote server, host:{}, port:{}", dstHost, dstPort);

                }

            }
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }
}
