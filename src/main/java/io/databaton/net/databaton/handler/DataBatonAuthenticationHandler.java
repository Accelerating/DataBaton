package io.databaton.net.databaton.handler;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonServerConfig;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonLoginMessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonAuthenticationHandler extends SimpleChannelInboundHandler<DataBatonLoginMessageProto.DataBatonLoginMessage> {

    private final DataBatonConfig dataBatonConfig;

    public DataBatonAuthenticationHandler(DataBatonConfig dataBatonConfig) {
        this.dataBatonConfig = dataBatonConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonLoginMessageProto.DataBatonLoginMessage msg) throws Exception {
        DataBatonServerConfig remoteServer = dataBatonConfig.getRemoteServer();
        String username = remoteServer.getUsername();
        String password = remoteServer.getPassword();

        if(username.equals(msg.getUsername()) && password.equals(msg.getPassword())){
            log.debug("authentication success");
            DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
            builder.setDstHost(msg.getDstHost());
            builder.setDstPort(msg.getDstPort());
            builder.setData(msg.getData());
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(builder.build());
        }else{
            log.warn("authentication failed");
            ctx.close();
        }
    }
}