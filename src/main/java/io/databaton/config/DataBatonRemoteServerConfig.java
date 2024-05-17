package io.databaton.config;

import lombok.Data;

@Data
public class DataBatonRemoteServerConfig {

    private String host;

    private int port;

    private String token;

}
