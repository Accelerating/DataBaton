package io.databaton.net.databaton.server;

import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.http.HttpTunnelProxyInitHandler;
import io.databaton.net.socks5.Socks5CommandRequestHandler;
import io.databaton.net.socks5.Socks5InitialRequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.util.ReferenceCountUtil;

/**
 * @author zxx
 */
public class LocalServerProxyProtocolDetectHandler extends ChannelInboundHandlerAdapter {

    private static final int MAX_PROTOCOL_DETECTION_BYTES = 64;

    private final DataBatonContext dataBatonContext;

    public LocalServerProxyProtocolDetectHandler(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;

        if (in.readableBytes() < 1) {
            return;
        }

        in.markReaderIndex();
        byte firstByte = in.getByte(0);

        if (firstByte == 0x05) {
            // socks5 proxy
            configureSocks5Pipeline(ctx);
        } else if (isHttpTunnel(in)) {
            // http tunnel
            configureHttpPipeline(ctx);
        } else {
            // other protocol
            ReferenceCountUtil.release(in);
            ctx.close();
            return;
        }

        in.resetReaderIndex();
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(msg);
    }

    private boolean isHttpTunnel(ByteBuf in) {
        if (in.readableBytes() < 3) {
            return false;
        }

        int magic1 = in.getUnsignedByte(in.readerIndex());
        int magic2 = in.getUnsignedByte(in.readerIndex() + 1);
        int magic3 = in.getUnsignedByte(in.readerIndex() + 2);

        // CONNECT
        return (magic1 == 'C' && magic2 == 'O' && magic3 == 'N');
    }

    private void configureSocks5Pipeline(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(Socks5ServerEncoder.DEFAULT);
        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(new Socks5InitialRequestHandler());
        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(new Socks5CommandRequestHandler(dataBatonContext));
    }

    private void configureHttpPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new HttpTunnelProxyInitHandler(dataBatonContext));
    }
}