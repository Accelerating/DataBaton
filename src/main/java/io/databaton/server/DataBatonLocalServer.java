package io.databaton.server;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonLocalServer {

    private final DataBatonConfig dataBatonConfig;
    private final CryptProcessor cryptProcessor;

    public DataBatonLocalServer(DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor) {
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
    }

    public void start(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup, NioEventLoopGroup clientGroup) throws Exception{
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DataBatonLocalServerHandlerInitializer(clientGroup, dataBatonConfig, cryptProcessor));
        int localServerPort = dataBatonConfig.getLocalServer().getPort();
        String proxyType = dataBatonConfig.getLocalServer().getProxyType();
        serverBootstrap.bind(localServerPort).addListener((future -> {
            if (future.isSuccess()) {
                log.info("local server start successfully, proxyType:{}, port: {}", proxyType, localServerPort);
            }
        })).sync();

    }

}
