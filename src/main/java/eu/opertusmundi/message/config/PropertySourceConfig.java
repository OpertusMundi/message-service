package eu.opertusmundi.message.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(ignoreResourceNotFound = true, value = {
    "classpath:git.properties"
})
public class PropertySourceConfig {

}
