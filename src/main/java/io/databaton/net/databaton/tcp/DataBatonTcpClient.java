package io.databaton.net.databaton.tcp;

import com.google.protobuf.ByteString;
import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.enums.ConnectionStatus;
import io.databaton.enums.OpType;
import io.databaton.exception.ConnectionException;
import io.databaton.net.databaton.DataBatonClient;
import io.databaton.net.databaton.tcp.codec.DataBatonTcpDecryptDecoder;
import io.databaton.net.databaton.tcp.codec.DataBatonTcpEncryptEncoder;
import io.databaton.net.databaton.tcp.handler.RemoteServerToLocalServerTcpHandler;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * A DataBaton client implemented using the TCP protocol
 * @author zxx
 */
@Slf4j
public class DataBatonTcpClient extends ChannelInboundHandlerAdapter implements DataBatonClient {

    private int status = ConnectionStatus.INIT;


    private final DataBatonContext dataBatonContext;
    private final ChannelHandlerContext clientCtx;
    private Channel toRemoteServerChannel;

    private final String dstHost;
    private final int dstPort;

    public DataBatonTcpClient(DataBatonContext dataBatonContext, ChannelHandlerContext clientCtx, String dstHost, int dstPort){
        this.dataBatonContext = dataBatonContext;
        this.clientCtx = clientCtx;
        this.dstHost = dstHost;
        this.dstPort = dstPort;
    }


    public void connectToRemoteServer() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(dataBatonContext.getClientGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DataBatonTcpDecryptDecoder(dataBatonContext));
                            ch.pipeline().addLast(new DataBatonTcpEncryptEncoder(dataBatonContext));
                            ch.pipeline().addLast(new RemoteServerToLocalServerTcpHandler(clientCtx.channel(), dataBatonContext));
                        }
                    });

            DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
            ChannelFuture future = bootstrap.connect(remoteServer.getHost(), remoteServer.getPort());
            if(future.sync().isSuccess()){
                this.toRemoteServerChannel = future.channel();
                return;
            }
        } catch (Exception e) {
            log.error("connect to remote server failed", e);
        }
        throw new ConnectionException("connect to remote server failed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try{
            if(isActive()){
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                if(status == ConnectionStatus.INIT){
                    DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
                    DataBatonLoginMessageProto.DataBatonLoginMessage.Builder builder = DataBatonLoginMessageProto.DataBatonLoginMessage.newBuilder();
                    builder.setToken(remoteServer.getToken());
                    builder.setDstHost(dstHost);
                    builder.setDstPort(dstPort);
                    builder.setData(ByteString.copyFrom(data));
                    status = ConnectionStatus.CONNECTED;
                    byte[] payload = builder.build().toByteArray();
                    sendData(new DataBatonMessage(OpType.LOGIN.genOperationTypeBytes(), payload));
                    log.debug("auth to remote server, host:{}, port:{}", dstHost, dstPort);
                }else if(status == ConnectionStatus.CONNECTED){
                    DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
                    builder.setDstHost(dstHost);
                    builder.setDstPort(dstPort);
                    builder.setData(ByteString.copyFrom(data));
                    byte[] payload = builder.build().toByteArray();
                    sendData(new DataBatonMessage(OpType.DISPATCH.genOperationTypeBytes(), payload));

                    log.debug("dispatch data to remote server, host:{}, port:{}", dstHost, dstPort);

                }

            }
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public boolean isActive() {
        return this.toRemoteServerChannel.isActive();
    }

    @Override
    public void sendData(Object data) {
        sendData(data, null);
    }

    @Override
    public void sendData(Object data, ChannelFutureListener listener) {
        ChannelFuture future = this.toRemoteServerChannel.writeAndFlush(data);
        if(listener != null){
            future.addListener(listener);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(toRemoteServerChannel != null && toRemoteServerChannel.isOpen()){
            toRemoteServerChannel.close();
        }
        super.channelInactive(ctx);
    }
}
