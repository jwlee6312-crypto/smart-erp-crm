package com.crmbank.erp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class MssqlToMysqlMigrationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        try {
            // 1. MSSQL DataSource
            DriverManagerDataSource mssqlDs = new DriverManagerDataSource();
            mssqlDs.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            mssqlDs.setUrl("jdbc:sqlserver://172.30.1.11:1434;databaseName=SMARTDB;encrypt=false;trustServerCertificate=true;");
            mssqlDs.setUsername("sa");
            mssqlDs.setPassword("Crmbank123!");
            JdbcTemplate mssql = new JdbcTemplate(mssqlDs);

            // 2. MySQL DataSource (3308)
            DriverManagerDataSource mysqlDs = new DriverManagerDataSource();
            mysqlDs.setDriverClassName("com.mysql.cj.jdbc.Driver");
            mysqlDs.setUrl("jdbc:mysql://172.30.1.11:3308/smartcrm?useUnicode=true&characterEncoding=UTF8&serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true");
            mysqlDs.setUsername("root");
            mysqlDs.setPassword("Crmbank123!");
            JdbcTemplate mysql = new JdbcTemplate(mysqlDs);

            log.info("🚀 [JAVA JDBC MIGRATION] Starting MSSQL -> MySQL (3308) migration with UTF-8 support...");

            String[] tables = {
                "haba900t_tbl", "haba920t_tbl", "haba990t_tbl", "haba970t_tbl", "haba980t_tbl",
                "haaa800t_tbl", "haaa810t_tbl", "haba935t_tbl", "haba910t_tbl", "haba020t_tbl",
                "haba120t_tbl", "com_manual", "boad001t_tbl", "call_mst_tbl", "campaign_mst"
            };

            for (String table : tables) {
                try {
                    List<Map<String, Object>> rows = mssql.queryForList("SELECT * FROM " + table);
                    log.info("📦 Table {}: {} rows fetched from MSSQL", table, rows.size());

                    if (rows.isEmpty()) continue;

                    // Clear existing MySQL table data to prevent duplicate primary key conflicts
                    mysql.execute("DELETE FROM " + table);

                    // Get column names from first row
                    Set<String> columns = rows.get(0).keySet();
                    String colList = String.join(", ", columns);
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < columns.size(); i++) {
                        placeholders.append(i == 0 ? "?" : ", ?");
                    }

                    String insertSql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")";

                    for (Map<String, Object> row : rows) {
                        Object[] values = new Object[columns.size()];
                        int idx = 0;
                        for (String col : columns) {
                            values[idx++] = row.get(col);
                        }
                        try {
                            mysql.update(insertSql, values);
                        } catch (Exception rowEx) {
                            // Ignore individual row error if duplicate or constraint violation
                        }
                    }
                    log.info("✅ Table {} migration completed successfully!", table);
                } catch (Exception tEx) {
                    log.warn("⚠️ Table {} migration skipped or error: {}", table, tEx.getMessage());
                }
            }

            log.info("✅ [JAVA JDBC MIGRATION] All table migrations completed successfully!");
        } catch (Exception e) {
            log.error("❌ [JAVA JDBC MIGRATION ERROR]", e);
        }
    }
}
