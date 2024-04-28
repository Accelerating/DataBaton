package io.databaton.utils;

public class RunUtils {

    public static void runIfSatisfy(boolean condition, Runnable task){
        if(condition){
            task.run();
        }
    }

}
