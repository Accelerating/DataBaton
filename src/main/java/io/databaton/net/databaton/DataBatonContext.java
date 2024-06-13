package io.databaton.net.databaton;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.enums.ProtocolType;
import io.databaton.enums.ServerMode;
import io.databaton.net.databaton.tcp.DataBatonTcpClient;
import io.databaton.net.databaton.server.DataBatonLocalServer;
import io.databaton.net.databaton.server.DataBatonRemoteServer;
import io.databaton.net.databaton.udp.DataBatonUdpClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * a context for the DataBaton application
 * @author zxx
 */
@Slf4j
@Data
@Component
public class DataBatonContext implements ApplicationContextAware {

    private ApplicationContext springCtx;
    private DataBatonConfig dataBatonConfig;
    private CryptProcessor cryptProcessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup clientGroup;


    public DataBatonContext(DataBatonConfig dataBatonConfig, CryptProcessor cryptProcessor){
        this.dataBatonConfig = dataBatonConfig;
        this.cryptProcessor = cryptProcessor;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(4);
        this.clientGroup = new NioEventLoopGroup(4);
    }

    @Override
    public void setApplicationContext(ApplicationContext springCtx) throws BeansException {
        this.springCtx = springCtx;
    }


    public DataBatonClient createDataBatonClient(ChannelHandlerContext clientCtx, String dstHost, int dstPost){
        String protocol = dataBatonConfig.getProtocol();
        if(ProtocolType.TCP.equals(protocol)){
            return new DataBatonTcpClient(this, clientCtx, dstHost, dstPost);
        }else if(ProtocolType.UDP.equals(protocol)){
            return new DataBatonUdpClient(this, clientCtx, dstHost, dstPost);
        }
        return null;
    }


    public DataBatonServer createDataBatonServer(){
        String mode = dataBatonConfig.getMode();
        if(ServerMode.LOCAL.equals(mode)){
            return new DataBatonLocalServer(this);

        }else if (ServerMode.REMOTE.equals(mode)) {
            return new DataBatonRemoteServer(this);
        }

        return null;
    }

}

