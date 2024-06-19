package io.databaton.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author zxx
 */
public class RandomUtils {

    public static byte getRandomByte(){
        return (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1);
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

    public static int getRandomInt(int start, int end) {
        if(start == end){
            return start;
        }
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }

    public static String getRandomStr(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static boolean getRandomBool(){
        int randomInt = getRandomInt(0, 1);
        return randomInt < 1;
    }

    public int selectRandomNum(int... numbers){
        int idx = getRandomInt(0, numbers.length - 1);
        return numbers[idx];
    }
}
