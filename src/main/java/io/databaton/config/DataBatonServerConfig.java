package io.databaton.config;

import lombok.Data;

@Data
public class DataBatonServerConfig {

    private String host;

    private int port;

    private String username;

    private String password;

}
