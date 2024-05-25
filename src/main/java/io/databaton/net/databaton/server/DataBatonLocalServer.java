package io.databaton.net.databaton.server;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.PacConfig;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.DataBatonServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonLocalServer implements DataBatonServer {

    private DataBatonContext dataBatonContext;

    public DataBatonLocalServer(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    public void start() throws Exception{
        DataBatonConfig dataBatonConfig = dataBatonContext.getDataBatonConfig();
        PacConfig.load(dataBatonConfig.getPac());
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(dataBatonContext.getBossGroup(), dataBatonContext.getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new DataBatonLocalServerHandlerInitializer(dataBatonContext));
        int localServerPort = dataBatonConfig.getLocalServer().getPort();
        String proxyType = dataBatonConfig.getLocalServer().getProxyType();
        serverBootstrap.bind(localServerPort).addListener((future -> {
            if (future.isSuccess()) {
                log.info("local server start successfully, proxyType:{}, port: {}", proxyType, localServerPort);
            }
        })).sync();

    }

}
