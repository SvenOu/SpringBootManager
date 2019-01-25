package com.sven.springbootmanager.main.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceCodeGeneratorConfig {
    private static final String DEFAULT_SERVER_PORT = "8080";
    public static final String URL_FORMATER = "http://localhost:%s/webs/security/login.html";

    @JsonProperty("jdbc.sqlite.url")
    private String jdbcSqliteUrl;

    /**
     * # 注意，路径一定要是反斜杠，而且末尾必须加反斜杠
     */
    @JsonProperty("sql-code-templates.baseRoot")
    private String sqlCodeTemplatesBaseRoot;

    @JsonProperty("server.port")
    private String serverPort;

    public String getJdbcSqliteUrl() {
        return jdbcSqliteUrl;
    }

    public void setJdbcSqliteUrl(String jdbcSqliteUrl) {
        this.jdbcSqliteUrl = jdbcSqliteUrl;
    }

    public String getSqlCodeTemplatesBaseRoot() {
        return sqlCodeTemplatesBaseRoot;
    }

    public void setSqlCodeTemplatesBaseRoot(String sqlCodeTemplatesBaseRoot) {
        this.sqlCodeTemplatesBaseRoot = sqlCodeTemplatesBaseRoot;
    }

    public String getServerPort() {
        return serverPort;
    }

    @JsonSetter("server.port")
    public void setServerPort(String serverPort) {
        if(serverPort == null || serverPort.length() <= 0){
            this.serverPort = DEFAULT_SERVER_PORT;
        }else {
            this.serverPort = serverPort;
        }
    }

    @Override
    public String toString() {
        return "SourceCodeGeneratorConfig{" +
                "jdbcSqliteUrl='" + jdbcSqliteUrl + '\'' +
                ", sqlCodeTemplatesBaseRoot='" + sqlCodeTemplatesBaseRoot + '\'' +
                ", serverPort='" + serverPort + '\'' +
                '}';
    }
}
