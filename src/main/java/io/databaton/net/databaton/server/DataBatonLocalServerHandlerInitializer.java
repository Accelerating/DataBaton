package io.databaton.net.databaton.server;

import io.databaton.net.databaton.DataBatonContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;


/**
 * @author zxx
 */
@Slf4j
public class DataBatonLocalServerHandlerInitializer extends ChannelInitializer<SocketChannel> {


    private final DataBatonContext dataBatonContext;

    public DataBatonLocalServerHandlerInitializer(DataBatonContext dataBatonContext){
        log.debug("new DataBatonLocalServerHandlerInitializer");
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LocalServerProxyProtocolDetectHandler(dataBatonContext));
//        String proxyType = dataBatonContext.getDataBatonConfig().getLocalServer().getProxyType();
//        if(ProxyType.HTTP.equals(proxyType)){
//            pipeline.addLast(new HttpServerCodec());
//            pipeline.addLast(new HttpObjectAggregator(65536));
//            pipeline.addLast(new HttpTunnelProxyInitHandler(dataBatonContext));
//        }else if(ProxyType.SOCKS5.equals(proxyType)){
//            pipeline.addLast(Socks5ServerEncoder.DEFAULT);
//            pipeline.addLast(new Socks5InitialRequestDecoder());
//            pipeline.addLast(new Socks5InitialRequestHandler());
//            pipeline.addLast(new Socks5CommandRequestDecoder());
//            pipeline.addLast(new Socks5CommandRequestHandler(dataBatonContext));
//        }

    }

}
