package com.crmbank.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
/**
 * 💡 [구조 최적화]
 * com.crmbank 하위의 모든 구성 요소(erp, crm, asterisk, config)를 자동으로 스캔합니다.
 * 이를 통해 403 Forbidden 에러 및 빈 주입 오류를 근본적으로 방지합니다.
 */
@ComponentScan(basePackages = "com.crmbank")
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> factory.addContextCustomizers(context -> context.setSessionTimeout(120)); // 120 minutes (2 hours)
    }
}
