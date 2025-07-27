package com.side.springtestbed.utils.utils;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class YugabyteDBYSQLContainer extends JdbcDatabaseContainer<YugabyteDBYSQLContainer> {
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("yugabytedb/yugabyte");
    private static final Integer YSQL_PORT = 5433;
    private static final Integer MASTER_DASHBOARD_PORT = 7000;
    private static final Integer TSERVER_DASHBOARD_PORT = 9000;
    private static final String JDBC_DRIVER_CLASS = "com.yugabyte.Driver";
    private static final String JDBC_CONNECT_PREFIX = "jdbc:yugabytedb";
    private static final String ENTRYPOINT = "bin/yugabyted start --background=false";
    private String database;
    private String username;
    private String password;

    public YugabyteDBYSQLContainer(String imageName) {
        this(DockerImageName.parse(imageName));
    }

    public YugabyteDBYSQLContainer(DockerImageName imageName) {
        super(imageName);
        this.database = "yugabyte";
        this.username = "yugabyte";
        this.password = "yugabyte";
        imageName.assertCompatibleWith(new DockerImageName[]{DEFAULT_IMAGE_NAME});
        this.withExposedPorts(new Integer[]{YSQL_PORT, MASTER_DASHBOARD_PORT, TSERVER_DASHBOARD_PORT});
        this.waitingFor((new YugabyteDBYSQLWaitStrategy(this)).withStartupTimeout(Duration.ofSeconds(60L)));
        this.withCommand("bin/yugabyted start --background=false");
    }

    public Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(this.getMappedPort(YSQL_PORT));
    }

    protected void configure() {
        this.addEnv("YSQL_DB", this.database);
        this.addEnv("YSQL_USER", this.username);
        this.addEnv("YSQL_PASSWORD", this.password);
    }

    public String getDriverClassName() {
        return "com.yugabyte.Driver";
    }

    public String getJdbcUrl() {
        return "jdbc:yugabytedb://" + this.getHost() + ":" + this.getMappedPort(YSQL_PORT) + "/" + this.database + this.constructUrlParameters("?", "&");
    }

    public String getDatabaseName() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getTestQueryString() {
        return "SELECT 1";
    }

    public YugabyteDBYSQLContainer withDatabaseName(String database) {
        this.database = database;
        return this;
    }

    public YugabyteDBYSQLContainer withUsername(String username) {
        this.username = username;
        return this;
    }

    public YugabyteDBYSQLContainer withPassword(String password) {
        this.password = password;
        return this;
    }
}
