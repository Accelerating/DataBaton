package io.databaton.net.databaton.tcp.model;

import lombok.Data;

import java.util.Arrays;

/**
 * DataBaton message protocol
 * |op1(1)|op2(1)|op3(1)|op4(1)|payloadLength(4)|payload|
 * @author zxx
 */
@Data
public class DataBatonMessage {

    public static final int HEADER_LENGTH = 8;


    /**
     * operation part1
     */
    private byte op1;

    /**
     * operation part2
     */
    private byte op2;

    /**
     * operation part3
     */
    private byte op3;

    /**
     * operation part4
     */
    private byte op4;

    /**
     * payload size
     */
    private int payloadLength;


    /**
     * data payload
     */
    private byte[] payload;

    public DataBatonMessage(byte[] ops, byte[] payload){
        this.op1 = ops[3];
        this.op2 = ops[2];
        this.op3 = ops[1];
        this.op4 = ops[0];
        this.payload = payload;
        this.payloadLength = payload.length;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.payloadLength = payload.length;
    }


    public byte[] getOperateTypeBytes(){
        return new byte[]{op4, op3, op2, op1};
    }


    public String toString(){
        byte[] opbs = new byte[]{op4, op3, op2, op1};
        return "opbs:" + Arrays.toString(opbs) + ", payloadLength:" + payloadLength;
    }

}
