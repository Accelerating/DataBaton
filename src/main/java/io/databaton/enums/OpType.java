package io.databaton.enums;

import io.databaton.utils.NumberUtils;
import io.databaton.utils.RandomUtils;
/**
 * @author zxx
 */
public enum OpType {

    /**
     * 0011
     */
    DISPATCH(3),

    /**
     * 0001
     */
    HEART_BEAT(1),

    /**
     * 0010
     */
    LOGIN(2),


    ;

    private int code;

    OpType(int code){
        this.code = code;
    }


    public byte[] genOperationTypeBytes(){
        int operationType = this.code;
        byte[] bytes = new byte[4];
        bytes[0] = (0b1000 & operationType) == 0b1000 ? RandomUtils.getRandomOddByte() : RandomUtils.getRandomEvenByte();
        bytes[1] = (0b0100 & operationType) == 0b0100 ? RandomUtils.getRandomOddByte() : RandomUtils.getRandomEvenByte();
        bytes[2] = (0b0010 & operationType) == 0b0010 ? RandomUtils.getRandomOddByte() : RandomUtils.getRandomEvenByte();
        bytes[3] = (0b0001 & operationType) == 0b0001 ? RandomUtils.getRandomOddByte() : RandomUtils.getRandomEvenByte();
        return bytes;
    }

    public static OpType translateOperationType(byte[] operationTypeBytes){
        int opb0 = NumberUtils.isOdd(operationTypeBytes[3]) ? 1 : 0;
        int opb1 = NumberUtils.isOdd(operationTypeBytes[2]) ? 1 : 0;
        int opb2 = NumberUtils.isOdd(operationTypeBytes[1]) ? 1 : 0;
        int opb3 = NumberUtils.isOdd(operationTypeBytes[0]) ? 1 : 0;
        int code = (opb3 << 3) + (opb2 << 2) + (opb1 << 1) + opb0;
        return findByCode(code);
    }

    public static OpType findByCode(int code){
        OpType[] values = values();
        for (OpType operationType : values) {
            if(operationType.code == code){
                return operationType;
            }
        }
        return null;
    }

}
