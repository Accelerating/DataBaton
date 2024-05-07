package io.databaton.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "databaton")
public class DataBatonConfig {

    private String mode;
    private String pac;
    private DataBatonCryptConfig crypt;
    private DataBatonServerConfig localServer;
    private DataBatonServerConfig remoteServer;

}
