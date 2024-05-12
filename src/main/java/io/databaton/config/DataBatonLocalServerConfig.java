package io.databaton.config;

import lombok.Data;

@Data
public class DataBatonLocalServerConfig {

    private String host;

    private int port;

    private String username;

    private String password;

    private String proxyType;

}
