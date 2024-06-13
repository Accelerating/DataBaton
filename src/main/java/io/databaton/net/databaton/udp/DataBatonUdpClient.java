package io.databaton.net.databaton.udp;

import io.databaton.net.databaton.DataBatonClient;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.tcp.codec.DataBatonTcpEncryptEncoder;
import io.databaton.net.databaton.tcp.handler.RemoteServerToLocalServerTcpHandler;
import io.databaton.net.databaton.udp.codec.DataBatonUdpDecryptDecoder;
import io.databaton.net.databaton.udp.codec.DataBatonUdpEncryptEncoder;
import io.databaton.net.databaton.udp.handler.RemoteServerToLocalServerUdpHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonUdpClient extends ChannelInboundHandlerAdapter implements DataBatonClient {

    private final DataBatonContext dataBatonContext;
    private final ChannelHandlerContext clientCtx;
    private final String targetHost;
    private final int targetPort;

    public DataBatonUdpClient(DataBatonContext dataBatonContext, ChannelHandlerContext clientCtx, String targetHost, int targetPort){
        this.dataBatonContext = dataBatonContext;
        this.clientCtx = clientCtx;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    public void connectToRemoteServer() {
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(dataBatonContext.getClientGroup())
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DataBatonUdpDecryptDecoder(dataBatonContext));
                            pipeline.addLast(new DataBatonUdpEncryptEncoder(dataBatonContext));
                            pipeline.addLast(new RemoteServerToLocalServerUdpHandler());
                        }
                    });
            bootstrap.bind(0).sync();
        }catch (Exception e){
            log.error("connect to remote server failed", e);
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void sendData(Object data) {

    }

    @Override
    public void sendData(Object data, ChannelFutureListener listener) {

    }

}
