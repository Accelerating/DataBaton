package io.databaton.config;

import lombok.Data;

/**
 * encrypt and decrypt config
 * @author zxx
 */
@Data
public class DataBatonCryptConfig {

    private int code;

    private String algorithm;

    private String key;

}
