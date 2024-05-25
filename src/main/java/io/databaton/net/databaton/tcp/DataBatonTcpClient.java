package io.databaton.net.databaton.tcp;

import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.enums.ConnectionStatus;
import io.databaton.net.databaton.DataBatonClient;
import io.databaton.net.databaton.tcp.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.tcp.codec.DataBatonEncryptEncoder;
import io.databaton.net.dispatch.RemoteServerToLocalServerHandler;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonTcpClient extends ChannelInboundHandlerAdapter implements DataBatonClient {


    private int state;

    private DataBatonContext dataBatonContext;
    private ChannelHandlerContext clientCtx;
    private Channel toRemoteServerChannel;

    public DataBatonTcpClient(DataBatonContext dataBatonContext, ChannelHandlerContext clientCtx){
        this.dataBatonContext = dataBatonContext;
        this.clientCtx = clientCtx;
        state = ConnectionStatus.INIT;
    }


    public ChannelFuture connectToRemoteServer() {

        if(this.state == ConnectionStatus.CONNECTED){
            return null;
        }
        this.state = ConnectionStatus.CONNECTED;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(dataBatonContext.getClientGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new DataBatonDecryptDecoder(dataBatonContext));
                        ch.pipeline().addLast(new DataBatonEncryptEncoder(dataBatonContext));
                        ch.pipeline().addLast(new RemoteServerToLocalServerHandler(clientCtx.channel(), dataBatonContext));
                    }
                });

        DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
        ChannelFuture future = bootstrap.connect(remoteServer.getHost(), remoteServer.getPort());
        toRemoteServerChannel = future.channel();
        return future;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(toRemoteServerChannel != null && toRemoteServerChannel.isOpen()){
            toRemoteServerChannel.close();
        }
        super.channelInactive(ctx);
    }
}
