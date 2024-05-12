package io.databaton.net.http;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.PacConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.dispatch.LocalClientToTargetServerHandler;
import io.databaton.net.dispatch.TargetServerToLocalClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
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

    private void proxyDispatch(String domain, int port, ChannelHandlerContext toClientCtx){

    }
}
