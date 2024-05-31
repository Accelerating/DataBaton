package io.databaton.net.databaton.server;

import io.databaton.enums.ProtocolType;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.DataBatonServer;
import io.databaton.net.databaton.tcp.DataBatonTcpServer;
import io.databaton.net.databaton.udp.DataBatonUdpServer;
import lombok.extern.slf4j.Slf4j;

/**
 * remote(proxy) server, used to decrypt data from local server and dispatch data to target server
 * @author zxx
 */
@Slf4j
public class DataBatonRemoteServer  implements DataBatonServer {

    private DataBatonContext dataBatonContext;

    public DataBatonRemoteServer(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    public void start() throws Exception {
        String protocol = dataBatonContext.getDataBatonConfig().getProtocol();
        if(ProtocolType.TCP.equals(protocol)){
            DataBatonTcpServer tcpServer = new DataBatonTcpServer(dataBatonContext);
            tcpServer.start();
        }else if(ProtocolType.UDP.equals(protocol)){
            DataBatonUdpServer udpServer = new DataBatonUdpServer();
            //todo
        }else{
            log.error("invalid protocol type:{}", protocol);
            System.exit(-1);
        }

    }

}
