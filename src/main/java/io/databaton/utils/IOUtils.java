package io.databaton.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

/**
 * @author zxx
 */
@Slf4j
public class IOUtils {

    public static void close(Closeable... resources){

        if(resources != null){
            for (Closeable resource : resources) {
                try{
                    resource.close();
                }catch (Exception e){
                    log.error("close resource failed, {}", resource, e);
                }

            }
        }

    }

}
