package com.side.springtestbed.utils.utils;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class OracleContainer extends JdbcDatabaseContainer<OracleContainer> {
    public static final String NAME = "oracle";
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("gvenzl/oracle-xe");
    static final String DEFAULT_TAG = "18.4.0-slim";
    static final String IMAGE;
    static final int ORACLE_PORT = 1521;
    private static final int APEX_HTTP_PORT = 8080;
    private static final int DEFAULT_STARTUP_TIMEOUT_SECONDS = 240;
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 120;
    static final String DEFAULT_DATABASE_NAME = "xepdb1";
    static final String DEFAULT_SID = "xe";
    static final String DEFAULT_SYSTEM_USER = "system";
    static final String DEFAULT_SYS_USER = "sys";
    static final String APP_USER = "test";
    static final String APP_USER_PASSWORD = "test";
    private static final List<String> ORACLE_SYSTEM_USERS;
    private String databaseName;
    private String username;
    private String password;
    private boolean usingSid;

    /** @deprecated */
    @Deprecated
    public OracleContainer() {
        this(DEFAULT_IMAGE_NAME.withTag("18.4.0-slim"));
    }

    public OracleContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public OracleContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.databaseName = "xepdb1";
        this.username = "test";
        this.password = "test";
        this.usingSid = false;
        dockerImageName.assertCompatibleWith(new DockerImageName[]{DEFAULT_IMAGE_NAME});
        this.preconfigure();
    }

    public OracleContainer(Future<String> dockerImageName) {
        super(dockerImageName);
        this.databaseName = "xepdb1";
        this.username = "test";
        this.password = "test";
        this.usingSid = false;
        this.preconfigure();
    }

    private void preconfigure() {
        this.waitStrategy = (new LogMessageWaitStrategy()).withRegEx(".*DATABASE IS READY TO USE!.*\\s").withTimes(1).withStartupTimeout(Duration.of(240L, ChronoUnit.SECONDS));
        this.withConnectTimeoutSeconds(120);
        this.addExposedPorts(new int[]{1521, 8080});
    }

    protected void waitUntilContainerStarted() {
        this.getWaitStrategy().waitUntilReady(this);
    }

    public @NotNull Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(this.getMappedPort(1521));
    }

    public String getDriverClassName() {
        return "oracle.jdbc.driver.OracleDriver";
    }

    public String getJdbcUrl() {
        return this.isUsingSid() ? "jdbc:oracle:thin:@" + this.getHost() + ":" + this.getOraclePort() + ":" + this.getSid() : "jdbc:oracle:thin:@" + this.getHost() + ":" + this.getOraclePort() + "/" + this.getDatabaseName();
    }

    public String getUsername() {
        return this.isUsingSid() ? "system" : this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    protected boolean isUsingSid() {
        return this.usingSid;
    }

    public OracleContainer withUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        } else if (ORACLE_SYSTEM_USERS.contains(username.toLowerCase())) {
            throw new IllegalArgumentException("Username cannot be one of " + ORACLE_SYSTEM_USERS);
        } else {
            this.username = username;
            return (OracleContainer)this.self();
        }
    }

    public OracleContainer withPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        } else {
            this.password = password;
            return (OracleContainer)this.self();
        }
    }

    public OracleContainer withDatabaseName(String databaseName) {
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        } else if ("xepdb1".equals(databaseName.toLowerCase())) {
            throw new IllegalArgumentException("Database name cannot be set to xepdb1");
        } else {
            this.databaseName = databaseName;
            return (OracleContainer)this.self();
        }
    }

    public OracleContainer usingSid() {
        this.usingSid = true;
        return (OracleContainer)this.self();
    }

    public OracleContainer withUrlParam(String paramName, String paramValue) {
        throw new UnsupportedOperationException("The Oracle Database driver does not support this");
    }

    public String getSid() {
        return "xe";
    }

    public Integer getOraclePort() {
        return this.getMappedPort(1521);
    }

    public Integer getWebPort() {
        return this.getMappedPort(8080);
    }

    public String getTestQueryString() {
        return "SELECT 1 FROM DUAL";
    }

    protected void configure() {
        this.withEnv("ORACLE_PASSWORD", this.password);
        if (this.databaseName != "xepdb1") {
            this.withEnv("ORACLE_DATABASE", this.databaseName);
        }

        this.withEnv("APP_USER", this.username);
        this.withEnv("APP_USER_PASSWORD", this.password);
    }

    static {
        IMAGE = DEFAULT_IMAGE_NAME.getUnversionedPart();
        ORACLE_SYSTEM_USERS = Arrays.asList("system", "sys");
    }
}

