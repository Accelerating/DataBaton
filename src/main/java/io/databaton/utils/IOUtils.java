package io.databaton.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public class IOUtils {

    public static void release(Closeable resource){
        try{
            if(resource != null){
                resource.close();
            }
        }catch (Exception e){
            log.error("release resource failed", e);
        }

    }

}
