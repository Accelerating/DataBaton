package io.databaton.net.databaton.udp.codec;

import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.net.databaton.udp.model.DataBatonDatagramPacket;
import io.databaton.net.databaton.udp.model.DataBatonUdpDispatchMessageProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zxx
 */
public class DataBatonUdpEncryptEncoder extends MessageToByteEncoder<DataBatonDatagramPacket> {

    private DataBatonContext dataBatonContext;

    public DataBatonUdpEncryptEncoder(DataBatonContext dataBatonContext) {
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DataBatonDatagramPacket msg, ByteBuf out) throws Exception {
        DataBatonUdpDispatchMessageProto.DataBatonUdpDispatchMessage message = msg.getMessage();
        byte[] data = message.toByteArray();
        CryptProcessor cryptProcessor = dataBatonContext.getCryptProcessor();
        byte[] encryptedData = cryptProcessor.encrypt(data);
        ByteBuf buffer = ctx.alloc().buffer(encryptedData.length);
        buffer.writeBytes(encryptedData);
        DatagramPacket packet = new DatagramPacket(buffer, msg.getTarget());
    }
}
