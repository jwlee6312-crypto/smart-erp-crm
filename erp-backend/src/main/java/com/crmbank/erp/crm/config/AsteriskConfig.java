package com.crmbank.erp.crm.config;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsteriskConfig {

    @Value("${asterisk.host:localhost}")
    private String host;

    @Value("${asterisk.port:5039}")
    private int port;

    @Value("${asterisk.username:admin}")
    private String username;

    @Value("${asterisk.password:Crmbank123!}")
    private String password;

    @Bean
    public ManagerConnection managerConnection() {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(host, port, username, password);
        return factory.createManagerConnection();
    }
}
