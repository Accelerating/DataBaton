package io.databaton.net.databaton.tcp.handler;

import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonDispatchHandler extends SimpleChannelInboundHandler<DataBatonDispatchMessageProto.DataBatonDispatchMessage> {

    private final DataBatonContext dataBatonContext;

    private Channel toTargetServerChannel;

    public DataBatonDispatchHandler(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception {
        log.debug("dispatch data to target server, host:{}, port:{}", msg.getDstHost(), msg.getDstPort());
        Channel channel = getToTargetServerChannel(ctx, msg);
        byte[] data = msg.getData().toByteArray();
        ByteBuf buf = ctx.alloc().buffer(data.length);
        buf.writeBytes(data);

        channel.writeAndFlush(buf);



    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        closeToTargetServerChannel();
        super.channelInactive(ctx);
    }

    private Channel getToTargetServerChannel(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception{
        if(this.toTargetServerChannel == null || !this.toTargetServerChannel.isActive()){
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(dataBatonContext.getClientGroup())
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TargetServerToRemoteServerHandler(ctx.channel(), dataBatonContext));
                        }
                    });
            ChannelFuture future = bootstrap.connect(msg.getDstHost(), msg.getDstPort()).sync();
            this.toTargetServerChannel = future.channel();
        }
        return this.toTargetServerChannel;
    }

    private void closeToTargetServerChannel(){
        try{
            this.toTargetServerChannel.close();
        }catch (Exception e){
            log.error("close channel failed: {}", this.toTargetServerChannel);
        }
    }

}