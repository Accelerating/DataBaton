package io.databaton.net.databaton.tcp;

import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.server.DataBatonRemoteServerHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonTcpServer {


    private DataBatonContext dataBatonContext;

    public DataBatonTcpServer(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    public void start() throws Exception {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(dataBatonContext.getBossGroup(), dataBatonContext.getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new DataBatonRemoteServerHandlerInitializer(dataBatonContext));
        int remoteServerPort = dataBatonContext.getDataBatonConfig().getRemoteServer().getPort();
        bootstrap.bind(remoteServerPort).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("data-baton server start successfully, port:{}", remoteServerPort);
            }
        }).sync();
    }

}
