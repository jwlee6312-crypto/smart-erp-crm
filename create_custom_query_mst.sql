USE smartcrm;

CREATE TABLE IF NOT EXISTS custom_query_mst (
    cmpycd      VARCHAR(10)   NOT NULL,
    query_id    VARCHAR(20)   NOT NULL,
    query_nm    VARCHAR(100)  NOT NULL,
    query_desc  VARCHAR(500)  NULL,
    query_json  TEXT          NULL,
    addtime     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updtime     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updemp      VARCHAR(10)   NULL,
    PRIMARY KEY (cmpycd, query_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
