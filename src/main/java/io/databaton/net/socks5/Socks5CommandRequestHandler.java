package io.databaton.net.socks5;

import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.config.PacConfig;
import io.databaton.net.databaton.DataBatonClient;
import io.databaton.net.databaton.tcp.handler.LocalServerToRemoteServerHandler;
import io.databaton.net.dispatch.LocalClientToTargetServerHandler;
import io.databaton.net.dispatch.TargetServerToLocalClientHandler;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private final DataBatonContext dataBatonContext;


    public Socks5CommandRequestHandler(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        if (!msg.type().equals(Socks5CommandType.CONNECT)) {
            log.debug("receive commandRequest type={}", msg.type());
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead(msg);
            return;
        }

        String host = msg.dstAddr();
        if(PacConfig.isProxyDomain(host)){
            proxyDispatch(ctx, msg);
        }else{
            directDispatch(ctx, msg);
        }
    }


    private void directDispatch(ChannelHandlerContext clientCtx, DefaultSocks5CommandRequest request) throws Exception {
        String targetHost = request.dstAddr();
        int targetPort = request.dstPort();
        Socks5AddressType socks5AddressType = request.dstAddrType();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(dataBatonContext.getClientGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TargetServerToLocalClientHandler(clientCtx.channel()));
                    }
                });

        bootstrap.connect(targetHost, targetPort).sync().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientCtx.pipeline().addLast(new LocalClientToTargetServerHandler(future.channel()));
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5AddressType);
                clientCtx.writeAndFlush(commandResponse);
                clientCtx.pipeline().remove(Socks5CommandRequestHandler.class);
                clientCtx.pipeline().remove(Socks5CommandRequestDecoder.class);
            } else {
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, socks5AddressType);
                clientCtx.writeAndFlush(commandResponse);
                future.channel().close();
            }
        });
    }

    private void proxyDispatch(ChannelHandlerContext clientCtx, DefaultSocks5CommandRequest request) throws Exception {
        String targetHost = request.dstAddr();
        int targetPort = request.dstPort();
        Socks5AddressType socks5AddressType = request.dstAddrType();
        DataBatonClient dataBatonClient = dataBatonContext.createDataBatonClient(clientCtx);
        // dispatch to remote server
        ChannelFuture future = dataBatonClient.connectToRemoteServer();
        DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
        if(future.sync().isSuccess()){
            log.debug("connect to remote server, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
            clientCtx.pipeline().addLast(new LocalServerToRemoteServerHandler(future.channel(), targetHost, targetPort, dataBatonContext));
            DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5AddressType);
            clientCtx.writeAndFlush(commandResponse);
            clientCtx.pipeline().remove(Socks5CommandRequestHandler.class);
            clientCtx.pipeline().remove(Socks5CommandRequestDecoder.class);
        }else{
            log.error("connect to remote server failed, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
            DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, socks5AddressType);
            clientCtx.writeAndFlush(commandResponse);
            clientCtx.close();
        }

    }

}
