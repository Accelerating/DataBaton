package io.databaton.config;

import lombok.Data;

/**
 * local server config
 * @author zxx
 */
@Data
public class DataBatonLocalServerConfig {

    private String host;

    private int port;

    private String username;

    private String password;

    private String proxyType;

}
