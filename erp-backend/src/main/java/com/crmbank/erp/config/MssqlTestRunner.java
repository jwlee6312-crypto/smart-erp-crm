package com.crmbank.erp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MssqlTestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            ds.setUrl("jdbc:sqlserver://172.30.1.11:1434;databaseName=SMARTDB;encrypt=false;trustServerCertificate=true;");
            ds.setUsername("sa");
            ds.setPassword("Crmbank123!");

            JdbcTemplate mssqlTemplate = new JdbcTemplate(ds);
            log.info("🧪 [MSSQL TEST] Querying HABA900T_TBL from MSSQL...");
            List<Map<String, Object>> rows = mssqlTemplate.queryForList("SELECT TOP 5 CODECD, CODENM FROM HABA900T_TBL");
            for (Map<String, Object> row : rows) {
                log.info("🧪 [MSSQL UNCORRUPTED CHECK] CODECD: {}, CODENM: {}", row.get("CODECD"), row.get("CODENM"));
            }
        } catch (Exception e) {
            log.error("❌ [MSSQL TEST ERROR]", e);
        }
    }
}
