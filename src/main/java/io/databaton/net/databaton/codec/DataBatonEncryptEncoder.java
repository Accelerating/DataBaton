package io.databaton.net.databaton.codec;

import io.databaton.config.DataBatonConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.net.databaton.model.DataBatonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBatonEncryptEncoder extends MessageToByteEncoder<DataBatonMessage>{

    private final CryptProcessor cryptProcessor;
    private final DataBatonConfig dataBatonConfig;

    public DataBatonEncryptEncoder(CryptProcessor cryptProcessor, DataBatonConfig dataBatonConfig){
        this.cryptProcessor = cryptProcessor;
        this.dataBatonConfig = dataBatonConfig;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, DataBatonMessage msg, ByteBuf out) throws Exception {

        byte[] payload = cryptProcessor.encrypt(msg.getPayload());


        out.writeByte(msg.getOp3());
        out.writeInt(payload.length);
        out.writeByte(msg.getOp1());
        out.writeByte(msg.getOp4());
        out.writeByte(msg.getOp2());
        out.writeBytes(payload);

        log.debug("write transitMessage --> {},", msg);

    }


}
