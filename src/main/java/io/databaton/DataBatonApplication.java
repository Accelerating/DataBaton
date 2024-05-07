package io.databaton;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.PacConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.enums.ServerMode;
import io.databaton.server.DataBatonLocalServer;
import io.databaton.server.DataBatonRemoteServer;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataBatonApplication implements CommandLineRunner {

    @Autowired
    private DataBatonConfig dataBatonConfig;
    @Autowired
    private CryptProcessor cryptProcessor;

    public static void main(String[] args) {
        SpringApplication.run(DataBatonApplication.class);

    }

    @Override
    public void run(String... args) throws Exception {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
        NioEventLoopGroup clientGroup = new NioEventLoopGroup(4);

        String mode = dataBatonConfig.getMode();
        if(ServerMode.LOCAL.equals(mode)){
            PacConfig.load(dataBatonConfig.getPac());
            DataBatonLocalServer transitLocalServer = new DataBatonLocalServer(dataBatonConfig, cryptProcessor);
            transitLocalServer.start(bossGroup, workerGroup, clientGroup);
        }
        if (ServerMode.REMOTE.equals(mode)) {
            DataBatonRemoteServer transitRemoteServer = new DataBatonRemoteServer(dataBatonConfig, cryptProcessor);
            transitRemoteServer.start(bossGroup, workerGroup, clientGroup);
        }
    }
}
