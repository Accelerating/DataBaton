package io.databaton.net.socks5;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * channel handler for socks connection initialization
 * @author zxx
 */
@Slf4j
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        boolean failure = msg.decoderResult().isFailure();
        if (failure) {
            log.error("init socks5 connection failed");
            ReferenceCountUtil.release(msg);
            ctx.channel().close();
            return;
        }

        Socks5InitialResponse socks5InitialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
        ctx.writeAndFlush(socks5InitialResponse);
        ctx.pipeline().remove(this);
        ctx.pipeline().remove(Socks5InitialRequestDecoder.class);

    }
}