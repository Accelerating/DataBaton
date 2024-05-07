package io.databaton.net.socks5;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonServerConfig;
import io.databaton.config.PacConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.codec.DataBatonEncryptEncoder;
import io.databaton.net.databaton.handler.LocalServerToRemoteServerHandler;
import io.databaton.net.databaton.handler.RemoteServerToLocalServerHandler;
import io.databaton.net.dispatch.LocalClientToTargetServerHandler;
import io.databaton.net.dispatch.TargetServerToLocalClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
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

    private NioEventLoopGroup clientGroup;
    private DataBatonConfig dataBatonConfig;
    private CryptProcessor cryptProcessor;

    public Socks5CommandRequestHandler(NioEventLoopGroup clientGroup, DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor){
        this.clientGroup = clientGroup;
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
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


    private void directDispatch(ChannelHandlerContext clientToLocalServerCtx, DefaultSocks5CommandRequest request) throws InterruptedException {
        String targetHost = request.dstAddr();
        int targetPort = request.dstPort();
        Socks5AddressType socks5AddressType = request.dstAddrType();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TargetServerToLocalClientHandler(clientToLocalServerCtx.channel()));
                    }
                });

        bootstrap.connect(targetHost, targetPort).sync().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientToLocalServerCtx.pipeline().addLast(new LocalClientToTargetServerHandler(future.channel()));
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5AddressType);
                clientToLocalServerCtx.writeAndFlush(commandResponse);
                clientToLocalServerCtx.pipeline().remove(Socks5CommandRequestHandler.class);
                clientToLocalServerCtx.pipeline().remove(Socks5CommandRequestDecoder.class);
            } else {
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, socks5AddressType);
                clientToLocalServerCtx.writeAndFlush(commandResponse);
                future.channel().close();
            }
        });
    }

    private void proxyDispatch(ChannelHandlerContext clientToLocalServerCtx, DefaultSocks5CommandRequest request) throws InterruptedException {
        String targetHost = request.dstAddr();
        int targetPort = request.dstPort();
        Socks5AddressType socks5AddressType = request.dstAddrType();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new DataBatonDecryptDecoder(cryptProcessor, dataBatonConfig));
                        ch.pipeline().addLast(new DataBatonEncryptEncoder(cryptProcessor, dataBatonConfig));
                        ch.pipeline().addLast(new RemoteServerToLocalServerHandler(clientToLocalServerCtx.channel(), dataBatonConfig));
                    }
                });

        DataBatonServerConfig remoteServer = dataBatonConfig.getRemoteServer();

        bootstrap.connect(remoteServer.getHost(), remoteServer.getPort()).sync().addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.debug("connect to remote server, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
                clientToLocalServerCtx.pipeline().addLast(new LocalServerToRemoteServerHandler(future.channel(), targetHost, targetPort, dataBatonConfig));
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5AddressType);
                clientToLocalServerCtx.writeAndFlush(commandResponse);
                clientToLocalServerCtx.pipeline().remove(Socks5CommandRequestHandler.class);
                clientToLocalServerCtx.pipeline().remove(Socks5CommandRequestDecoder.class);
            }else{
                log.error("connect to remote server failed, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, socks5AddressType);
                clientToLocalServerCtx.writeAndFlush(commandResponse);
                future.channel().close();
            }
        });

    }

}
