package io.databaton.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application config
 * @author zxx
 */
@Data
@Component
@ConfigurationProperties(prefix = "databaton")
public class DataBatonConfig {

    private String mode;
    private String protocol;
    private String pac;
    private DataBatonCryptConfig crypt;
    private DataBatonLocalServerConfig localServer;
    private DataBatonRemoteServerConfig remoteServer;

}
