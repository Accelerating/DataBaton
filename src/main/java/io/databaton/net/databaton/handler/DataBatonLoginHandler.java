package io.databaton.net.databaton.handler;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonServerConfig;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonLoginMessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DataBatonLoginHandler  extends SimpleChannelInboundHandler<DataBatonLoginMessageProto.DataBatonLoginMessage> {

    private DataBatonConfig dataBatonConfig;

    public DataBatonLoginHandler(DataBatonConfig dataBatonConfig) {
        this.dataBatonConfig = dataBatonConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonLoginMessageProto.DataBatonLoginMessage msg) throws Exception {
        DataBatonServerConfig remoteServer = dataBatonConfig.getRemoteServer();
        String username = remoteServer.getUsername();
        String password = remoteServer.getPassword();

        if(username.equals(msg.getUsername()) && password.equals(msg.getPassword())){
            DataBatonDispatchMessageProto.DataBatonDispatchMessage.Builder builder = DataBatonDispatchMessageProto.DataBatonDispatchMessage.newBuilder();
            builder.setDstHost(msg.getDstHost());
            builder.setDstPort(msg.getDstPort());
            builder.setData(msg.getData());
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(builder.build());
        }else{
            ctx.close();
        }
    }
}