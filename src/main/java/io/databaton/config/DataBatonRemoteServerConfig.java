package io.databaton.config;

import lombok.Data;

/**
 * remote(proxy) server config
 * @author zxx
 */
@Data
public class DataBatonRemoteServerConfig {

    private String host;

    private int port;

    private String token;

}
