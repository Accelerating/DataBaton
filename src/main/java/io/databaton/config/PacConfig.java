package io.databaton.config;

import io.databaton.utils.IOUtils;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pac config
 * @author zxx
 */
@Slf4j
@Data
public class PacConfig {

    private static PacConfig INSTANCE;

    private String pacFilePath;

    private List<Pattern> direct;
    private List<Pattern> proxy;

    public PacConfig(){}
    public PacConfig(String pacFilePath){
        this.pacFilePath = pacFilePath;
    }


    public static void load(String pacFilePath){
        INSTANCE = new PacConfig(pacFilePath);
        INSTANCE.refresh();
    }

    public static boolean isProxyDomain(String domain){
        if(INSTANCE.proxy != null){
            for (Pattern pattern : INSTANCE.proxy) {

                Matcher matcher = pattern.matcher(domain);
                if(matcher.matches()){
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void refresh(){
        if(StringUtils.isEmpty(pacFilePath)){
            return;
        }

        InputStream in = null;
        try{
            in = new FileInputStream(pacFilePath);
            Map cfgMap = new Yaml().load(in);
            List<String> directDomainList = (List<String>) cfgMap.get("direct");
            List<String> proxyDomainList = (List<String>) cfgMap.get("proxy");
            this.direct = convertToDomainPattern(directDomainList);
            this.proxy = convertToDomainPattern(proxyDomainList);
        }catch (Exception e){
            log.error("reload pac config failed", e);
        }finally {
            IOUtils.close(in);
        }


    }

    private List<Pattern> convertToDomainPattern(List<String> domainRules){
        List<Pattern> patterns = new ArrayList<>();
        for (String domainRule : domainRules) {
            String regex = domainRule.replace(".", "\\.").replace("*", ".*");
            Pattern pattern = Pattern.compile(regex);
            patterns.add(pattern);
        }
        return patterns;
    }

}
