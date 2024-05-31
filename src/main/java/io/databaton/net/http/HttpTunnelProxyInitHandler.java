package io.databaton.net.http;

import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.config.PacConfig;
import io.databaton.net.databaton.DataBatonClient;
import io.databaton.net.databaton.tcp.codec.DataBatonDecryptDecoder;
import io.databaton.net.databaton.tcp.codec.DataBatonEncryptEncoder;
import io.databaton.net.databaton.tcp.handler.LocalServerToRemoteServerHandler;
import io.databaton.net.dispatch.RemoteServerToLocalServerHandler;
import io.databaton.net.dispatch.LocalClientToTargetServerHandler;
import io.databaton.net.dispatch.TargetServerToLocalClientHandler;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;


/**
 * @author zxx
 */
@Slf4j
public class HttpTunnelProxyInitHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private DataBatonContext dataBatonContext;

    public HttpTunnelProxyInitHandler(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;

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
        ChannelFuture future = bootstrap.group(dataBatonContext.getClientGroup())
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

    private void proxyDispatch(String targetHost, int targetPort, ChannelHandlerContext clientCtx) throws InterruptedException {

        DataBatonClient dataBatonClient = dataBatonContext.createDataBatonClient(clientCtx);
        DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();

        ChannelFuture future = dataBatonClient.connectToRemoteServer();
        if(future.sync().isSuccess()){
            log.debug("connect to remote server, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            clientCtx.writeAndFlush(resp);
            clientCtx.pipeline().remove(HttpTunnelProxyInitHandler.class);
            clientCtx.pipeline().remove(HttpServerCodec.class);
            clientCtx.pipeline().remove(HttpObjectAggregator.class);
            clientCtx.pipeline().addLast(new LocalServerToRemoteServerHandler(future.channel(), targetHost, targetPort, dataBatonContext));
        }else{
            log.error("connect to remote server failed, host:{}, port:{}", remoteServer.getHost(), remoteServer.getPort());
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            clientCtx.writeAndFlush(resp);
            future.channel().close();
        }

    }
}
