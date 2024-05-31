package io.databaton.net.databaton.tcp.handler;

import com.google.protobuf.ByteString;
import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonMessage;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * local server ---> remote(proxy) server
 * @author zxx
 */
@Slf4j
public class LocalServerToRemoteServerHandler extends ChannelInboundHandlerAdapter {

    private static final int STATE_INIT = 0;
    private static final int STATE_CONNECTED = 1;
    private int status = STATE_INIT;

    private final DataBatonContext dataBatonContext;
    private final Channel toRemoteServerChannel;
    private final String dstHost;
    private final int dstPort;

    public LocalServerToRemoteServerHandler(Channel toRemoteServerChannel, String dstHost, int dstPort, DataBatonContext dataBatonContext){
        this.toRemoteServerChannel = toRemoteServerChannel;
        this.dstHost = dstHost;
        this.dstPort = dstPort;
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try{
            if(toRemoteServerChannel.isActive()){
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                if(status == STATE_INIT){
                    DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
                    DataBatonLoginMessageProto.DataBatonLoginMessage.Builder builder = DataBatonLoginMessageProto.DataBatonLoginMessage.newBuilder();
                    builder.setToken(remoteServer.getToken());
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
