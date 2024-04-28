package io.databaton.utils;

public class NumberUtils {

    public static boolean isOdd(byte num){
        return num % 2 != 0;
    }


    public static boolean isEven(byte num){
        return !isOdd(num);
    }

}
