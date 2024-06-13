package io.databaton.net.databaton.udp.codec;

import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.udp.model.DataBatonUdpDispatchMessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * decrypt decoder
 * @author zxx
 */
public class DataBatonUdpDecryptDecoder extends SimpleChannelInboundHandler<DatagramPacket> {

    private DataBatonContext dataBatonContext;

    public DataBatonUdpDecryptDecoder(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        try{
            int length = buf.readableBytes();
            byte[] data = new byte[length];
            buf.readBytes(data);
            CryptProcessor cryptProcessor = dataBatonContext.getCryptProcessor();
            byte[] decryptedData = cryptProcessor.decrypt(data);
            DataBatonUdpDispatchMessageProto.DataBatonUdpDispatchMessage message = DataBatonUdpDispatchMessageProto.DataBatonUdpDispatchMessage.parseFrom(decryptedData);
            ctx.fireChannelRead(message);
        }finally {
            ReferenceCountUtil.release(buf);
        }

    }
}
