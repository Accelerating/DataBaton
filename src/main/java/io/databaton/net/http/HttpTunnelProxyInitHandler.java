package io.databaton.net.http;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.config.PacConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.codec.DataBatonEncryptEncoder;
import io.databaton.net.databaton.handler.LocalServerToRemoteServerHandler;
import io.databaton.net.databaton.handler.RemoteServerToLocalServerHandler;
import io.databaton.net.dispatch.LocalClientToTargetServerHandler;
import io.databaton.net.dispatch.TargetServerToLocalClientHandler;
import io.databaton.net.socks5.Socks5CommandRequestHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpTunnelProxyInitHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private NioEventLoopGroup clientGroup;
    private DataBatonConfig dataBatonConfig;
    private CryptProcessor cryptProcessor;

    public HttpTunnelProxyInitHandler(NioEventLoopGroup clientGroup, DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor) {
        this.clientGroup = clientGroup;
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        HttpMethod method = request.method();
        if(method == HttpMethod.CONNECT){
            String host = request.headers().get(HttpHeaderNames.HOST);
            String[] parts = host.split(":");
            String domain = parts[0];
            int port = Integer.parseInt(parts[1]);
            if (PacConfig.isProxyDomain(host)) {
                proxyDispatch(domain, port, ctx);
            }else{
                directDispatch(domain, port, ctx);
            }



        }
    }


    private void directDispatch(String domain, int port, ChannelHandlerContext toClientCtx) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture future = bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new TargetServerToLocalClientHandler(toClientCtx.channel()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .connect(domain, port).sync();

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if(cf.isSuccess()){
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    toClientCtx.writeAndFlush(resp);
                    ChannelPipeline pipeline = toClientCtx.pipeline();
                    pipeline.remove(HttpTunnelProxyInitHandler.class);
                    pipeline.remove(HttpServerCodec.class);
                    pipeline.remove(HttpObjectAggregator.class);
                    pipeline.addLast(new LocalClientToTargetServerHandler(cf.channel()));
                }else{
                    toClientCtx.close();
                }
            }
        });

    }

    private void proxyDispatch(String targetHost, int targetPort, ChannelHandlerContext clientToLocalServerCtx) throws InterruptedException {

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

        DataBatonRemoteServerConfig remoteServer = dataBatonConfig.getRemoteServer();

        bootstrap.connect(remoteServer.getHost(), remoteServer.getPort()).sync().addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.debug("connect to remote server, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                clientToLocalServerCtx.writeAndFlush(resp);
                clientToLocalServerCtx.pipeline().remove(HttpTunnelProxyInitHandler.class);
                clientToLocalServerCtx.pipeline().remove(HttpServerCodec.class);
                clientToLocalServerCtx.pipeline().remove(HttpObjectAggregator.class);
                clientToLocalServerCtx.pipeline().addLast(new LocalServerToRemoteServerHandler(future.channel(), targetHost, targetPort, dataBatonConfig));
            }else{
                log.error("connect to remote server failed, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                clientToLocalServerCtx.writeAndFlush(resp);
                future.channel().close();
            }
        });
    }
}