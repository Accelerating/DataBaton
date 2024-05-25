package io.databaton.net.databaton.server;

import io.databaton.enums.ProxyType;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.http.HttpTunnelProxyInitHandler;
import io.databaton.net.socks5.Socks5CommandRequestHandler;
import io.databaton.net.socks5.Socks5InitialRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;

public class DataBatonLocalServerHandlerInitializer extends ChannelInitializer<SocketChannel> {


    private final DataBatonContext dataBatonContext;

    public DataBatonLocalServerHandlerInitializer(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        String proxyType = dataBatonContext.getDataBatonConfig().getLocalServer().getProxyType();
        if(ProxyType.HTTP.equals(proxyType)){
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new HttpTunnelProxyInitHandler(dataBatonContext));
        }else if(ProxyType.SOCKS5.equals(proxyType)){
            pipeline.addLast(Socks5ServerEncoder.DEFAULT);
            pipeline.addLast(new Socks5InitialRequestDecoder());
            pipeline.addLast(new Socks5InitialRequestHandler());
            pipeline.addLast(new Socks5CommandRequestDecoder());
            pipeline.addLast(new Socks5CommandRequestHandler(dataBatonContext));
        }

    }

}
