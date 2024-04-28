package io.databaton.net.databaton.codec;

import io.databaton.crypt.CryptProcessor;
import io.databaton.enums.OpType;
import io.databaton.net.databaton.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.model.DataBatonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DataBatonDecryptDecoder extends ByteToMessageDecoder {

    private final CryptProcessor cryptProcessor;

    public DataBatonDecryptDecoder(CryptProcessor cryptProcessor){
        this.cryptProcessor = cryptProcessor;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < DataBatonMessage.HEADER_LENGTH){
            return;
        }

        in.markReaderIndex();


        byte op3 = in.readByte();
        int payloadLength = in.readInt();
        byte op1 = in.readByte();
        byte op4 = in.readByte();
        byte op2 = in.readByte();

        if(in.readableBytes() < payloadLength){
            in.resetReaderIndex();
            return;
        }
        byte[] ops = new byte[]{op4, op3, op2, op1};
        byte[] payload = new byte[payloadLength];

//        log.debug("read transitMessage --> opbs:{}, payloadLength:{}", Arrays.toString(ops), payloadLength);

        in.readBytes(payload);

        //decrypt
        payload = cryptProcessor.decrypt(payload);

        OpType opType = OpType.translateOperationType(ops);
        if(opType == OpType.LOGIN){
            DataBatonLoginMessageProto.DataBatonLoginMessage message = DataBatonLoginMessageProto.DataBatonLoginMessage.parseFrom(payload);
            out.add(message);
        }else if(opType == OpType.DISPATCH){
            DataBatonDispatchMessageProto.DataBatonDispatchMessage message = DataBatonDispatchMessageProto.DataBatonDispatchMessage.parseFrom(payload);
            out.add(message);
        }
    }
}
