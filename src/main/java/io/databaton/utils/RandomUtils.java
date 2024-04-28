package io.databaton.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static byte getRandomByte(){
        return (byte) ThreadLocalRandom.current().nextInt(0, Byte.MAX_VALUE + 1);
    }

    public static byte getRandomOddByte() {
        int num = getRandomByte();
        if (num % 2 == 0) {
            num = num + 1;
        }
        return (byte) num;
    }

    public static byte getRandomEvenByte(){
        return (byte) (getRandomOddByte() + 1);
    }

    public static long getRandomNum(int start, int end) {
        if(start == end){
            return start;
        }
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }
}
