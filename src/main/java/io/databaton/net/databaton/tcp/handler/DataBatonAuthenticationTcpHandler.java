package io.databaton.net.databaton.tcp.handler;

import io.databaton.config.DataBatonRemoteServerConfig;
import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication handler, once authentication is successful, this handler will be removed from the pipeline
 * @author zxx
 */
@Slf4j
public class DataBatonAuthenticationTcpHandler extends SimpleChannelInboundHandler<DataBatonLoginMessageProto.DataBatonLoginMessage> {

    private final DataBatonContext dataBatonContext;

    public DataBatonAuthenticationTcpHandler(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonLoginMessageProto.DataBatonLoginMessage msg) throws Exception {
        DataBatonRemoteServerConfig remoteServer = dataBatonContext.getDataBatonConfig().getRemoteServer();
        String token = remoteServer.getToken();

        if(token.equals(msg.getToken())){
            log.debug("authentication success");
            DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
            builder.setDstHost(msg.getDstHost());
            builder.setDstPort(msg.getDstPort());
            builder.setData(msg.getData());
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(builder.build());
        }else{
            log.error("authentication failed");
            ctx.close();
        }
    }
}