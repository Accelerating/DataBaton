package io.databaton.net.databaton.tcp.codec;

import io.databaton.enums.OpType;
import io.databaton.net.databaton.tcp.model.DataBatonDispatchMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonLoginMessageProto;
import io.databaton.net.databaton.tcp.model.DataBatonMessage;
import io.databaton.net.databaton.DataBatonContext;
import io.databaton.utils.NumberUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;


/**
 * decrypt decoder
 * @author zxx
 */
@Slf4j
public class DataBatonTcpDecryptDecoder extends ByteToMessageDecoder {

    private final DataBatonContext dataBatonContext;

    public DataBatonTcpDecryptDecoder(DataBatonContext dataBatonContext){
        this.dataBatonContext = dataBatonContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("new connection");
        super.channelActive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < DataBatonMessage.HEADER_LENGTH){
            return;
        }

        in.markReaderIndex();


        byte op0 = in.readByte();
        byte op1 = in.readByte();
        byte op2 = in.readByte();
        byte op3 = in.readByte();
        int payloadLength = in.readInt();
        if(in.readableBytes() < payloadLength){
            in.resetReaderIndex();
            return;
        }
        byte[] ops = new byte[]{op3, op2, op1, op0};
        byte[] payload = new byte[payloadLength];
        log.debug("read data baton message --> opbs:{}, payloadLengths:{}", Arrays.toString(ops), payloadLength);


        in.readBytes(payload);

        //decrypt
        payload = dataBatonContext.getCryptProcessor().decrypt(payload);

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
