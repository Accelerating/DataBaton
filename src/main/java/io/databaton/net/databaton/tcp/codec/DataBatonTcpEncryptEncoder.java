package io.databaton.net.databaton.tcp.codec;

import io.databaton.net.databaton.tcp.model.DataBatonMessage;
import io.databaton.net.databaton.DataBatonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;


/**
 * encrypt encoder
 * @author zxx
 */
@Slf4j
public class DataBatonTcpEncryptEncoder extends MessageToByteEncoder<DataBatonMessage>{

    private DataBatonContext dataBatonContext;

    public DataBatonTcpEncryptEncoder(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, DataBatonMessage msg, ByteBuf out) throws Exception {

        byte[] payload = dataBatonContext.getCryptProcessor().encrypt(msg.getPayload());


        out.writeByte(msg.getOp1());
        out.writeByte(msg.getOp2());
        out.writeByte(msg.getOp3());
        out.writeByte(msg.getOp4());
        out.writeInt(payload.length);
        out.writeBytes(payload);

        log.debug("write data baton message --> {},", msg);

    }


}
