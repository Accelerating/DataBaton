package io.databaton.net.databaton.model;

import lombok.Data;

import java.util.Arrays;

@Data
public class DataBatonMessage {

    public static final int HEADER_LENGTH = 8;

    /**
     * operation part3
     */
    private byte op3;
    public static final int OP3_IDX = 0;


    /**
     * payload size
     */
    private int payloadLength;
    public static final int PAYLOAD_LENGTH_IDX = 1;


    /**
     * operation part1
     */
    private byte op1;
    public static final int OP1_IDX = 5;

    /**
     * operation part3
     */
    private byte op4;
    public static final int OP4_IDX = 6;

    /**
     * operation part2
     */
    private byte op2;
    public static final int OP2_IDX = 7;



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
