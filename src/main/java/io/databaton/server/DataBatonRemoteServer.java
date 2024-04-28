package io.databaton.server;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonRemoteServer {

    private final DataBatonConfig dataBatonConfig;
    private final CryptProcessor cryptProcessor;

    public DataBatonRemoteServer(DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor) {
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
    }

    public void start(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup, NioEventLoopGroup clientGroup) throws Exception {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DataBatonRemoteServerHandlerInitializer(clientGroup, dataBatonConfig, cryptProcessor));
        int remoteServerPort = dataBatonConfig.getRemoteServer().getPort();
        bootstrap.bind(remoteServerPort).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("data-baton server start successfully, port:{}", remoteServerPort);
            }
        }).sync();
    }

}
