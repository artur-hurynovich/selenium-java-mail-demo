package com.gpsolutions.selenium_demo.main.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class SeleniumDemoAppConfiguration {
    @Value("${mail.ru.protocol.key}")
    private String protocolKey;
    @Value("${mail.ru.protocol.value}")
    private String protocolValue;

    @Bean
    public Properties mailRuProperties() {
        final Properties mailRuProperties = new Properties();
        mailRuProperties.setProperty(protocolKey, protocolValue);
        return mailRuProperties;
    }
}
