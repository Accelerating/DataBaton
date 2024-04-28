package io.databaton.net.databaton.handler;

import io.databaton.config.DataBatonConfig;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.utils.RunUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonDispatchHandler extends SimpleChannelInboundHandler<DataBatonDispatchMessageProto.DataBatonDispatchMessage> {

    private final NioEventLoopGroup clientGroup;

    private final DataBatonConfig dataBatonConfig;

    private Channel toTargetServerChannel;

    public DataBatonDispatchHandler(NioEventLoopGroup clientGroup, DataBatonConfig dataBatonConfig){
        this.clientGroup = clientGroup;
        this.dataBatonConfig = dataBatonConfig;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception {
        RunUtils.runIfSatisfy(dataBatonConfig.getDebug(), ()->{
            log.info("dispatch data to target server, host:{}, port:{}", msg.getDstHost(), msg.getDstPort());
        });
        Channel channel = getChannel(ctx, msg);
        byte[] data = msg.getData().toByteArray();
        ByteBuf buf = ctx.alloc().buffer(data.length);
        buf.writeBytes(data);
        channel.writeAndFlush(buf);

    }


    private Channel getChannel(ChannelHandlerContext ctx, DataBatonDispatchMessageProto.DataBatonDispatchMessage msg) throws Exception{
        if(this.toTargetServerChannel == null || !this.toTargetServerChannel.isActive()){
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(clientGroup)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TargetServerToRemoteServerHandler(ctx.channel(), dataBatonConfig));
                        }
                    });
            ChannelFuture future = bootstrap.connect(msg.getDstHost(), msg.getDstPort()).sync();
            this.toTargetServerChannel = future.channel();
        }
        return this.toTargetServerChannel;
    }

}