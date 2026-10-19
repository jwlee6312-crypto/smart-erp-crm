package com.crmbank.erp.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import com.crmbank.erp.global.handler.MapKeyLowerWrapperFactory;
import com.zaxxer.hikari.HikariDataSource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
/**
 * 💡 ERP (MySQL) Data Source Configuration
 * [중요] asterisk와 hgpa 패키지를 제외하여 MySQL 매퍼와의 충돌을 방지합니다.
 */
@MapperScan(
    basePackages = "com.crmbank.erp",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.crmbank\\.erp\\.(asterisk|hgpa)\\..*"
    ),
    sqlSessionFactoryRef = "erpSqlSessionFactory"
)
public class ErpDataSourceConfig {

    @Autowired
    private Environment env;

    @Bean(name = "erpDataSource")
    @Primary
    public DataSource erpDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        String host = env.getProperty("ERP_DB_HOST", "172.30.1.11");
        String port = env.getProperty("ERP_DB_PORT", "3308");
        String dbName = env.getProperty("ERP_DB_NAME", "smartcrm");
        String username = env.getProperty("ERP_DB_USERNAME", "root");
        String password = env.getProperty("ERP_DB_PASSWORD", "Crmbank123!");
        
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF8&serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true",
                host, port, dbName);
        
        log.info("🔌 [MySQL ERP DB - CRM] Connecting to URL: {}, Host: {}, Port: {}, DB: {}, User: {}", jdbcUrl, host, port, dbName, username);
        
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setPoolName("ErpHikariPool");
        dataSource.setMaximumPoolSize(10);
        
        return dataSource;
    }

    @Bean(name = "erpJdbcTemplate")
    public JdbcTemplate erpJdbcTemplate(@Qualifier("erpDataSource") DataSource erpDataSource) {
        return new JdbcTemplate(erpDataSource);
    }

    @Bean(name = "erpSqlSessionFactory")
    @Primary
    public SqlSessionFactory erpSqlSessionFactory(@Qualifier("erpDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        // 💡 [중요] asterisk와 hgpa 폴더를 제외한 모든 XML 매퍼 로드
        org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:/com/crmbank/erp/**/*.xml");
        List<org.springframework.core.io.Resource> filteredResources = new ArrayList<>();
        
        for (org.springframework.core.io.Resource res : resources) {
            String path = res.getURL().toString();
            if (!path.contains("/asterisk/") && !path.contains("/hgpa/")) {
                filteredResources.add(res);
            }
        }
        
        sessionFactory.setMapperLocations(filteredResources.toArray(new org.springframework.core.io.Resource[0]));
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setCallSettersOnNulls(true);
        configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(false);
        configuration.setObjectWrapperFactory(new MapKeyLowerWrapperFactory());

        sessionFactory.setConfiguration(configuration);
        return sessionFactory.getObject();
    }

    @Bean(name = "erpSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate erpSqlSessionTemplate(
            @Qualifier("erpSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "erpTransactionManager")
    @Primary
    public PlatformTransactionManager erpTransactionManager(@Qualifier("erpDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
