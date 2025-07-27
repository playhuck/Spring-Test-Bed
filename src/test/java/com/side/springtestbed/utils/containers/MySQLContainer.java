package com.side.springtestbed.utils.containers;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

public class MySQLContainer<SELF extends MySQLContainer<SELF>> extends JdbcDatabaseContainer<SELF> {
    public static final String NAME = "mysql";
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("mysql");
    /** @deprecated */
    @Deprecated
    public static final String DEFAULT_TAG = "5.7.34";
    /** @deprecated */
    @Deprecated
    public static final String IMAGE;
    static final String DEFAULT_USER = "test";
    static final String DEFAULT_PASSWORD = "test";
    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";
    public static final Integer MYSQL_PORT;
    private String databaseName;
    private String username;
    private String password;
    private static final String MYSQL_ROOT_USER = "root";

    /** @deprecated */
    @Deprecated
    public MySQLContainer() {
        this(DEFAULT_IMAGE_NAME.withTag("5.7.34"));
    }

    public MySQLContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public MySQLContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.databaseName = "test";
        this.username = "test";
        this.password = "test";
        dockerImageName.assertCompatibleWith(new DockerImageName[]{DEFAULT_IMAGE_NAME});
        this.addExposedPort(MYSQL_PORT);
    }

    /** @deprecated */
    @Deprecated
    protected @NotNull Set<Integer> getLivenessCheckPorts() {
        return super.getLivenessCheckPorts();
    }

    protected void configure() {
        this.optionallyMapResourceParameterAsVolume("TC_MY_CNF", "/etc/mysql/conf.d", "mysql-default-conf", 16877);
        this.addEnv("MYSQL_DATABASE", this.databaseName);
        if (!"root".equalsIgnoreCase(this.username)) {
            this.addEnv("MYSQL_USER", this.username);
        }

        if (this.password != null && !this.password.isEmpty()) {
            this.addEnv("MYSQL_PASSWORD", this.password);
            this.addEnv("MYSQL_ROOT_PASSWORD", this.password);
        } else {
            if (!"root".equalsIgnoreCase(this.username)) {
                throw new ContainerLaunchException("Empty password can be used only with the root user");
            }

            this.addEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes");
        }

        this.setStartupAttempts(3);
    }

    public String getDriverClassName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException var2) {
            return "com.mysql.jdbc.Driver";
        }
    }

    public String getJdbcUrl() {
        String additionalUrlParams = this.constructUrlParameters("?", "&");
        return "jdbc:mysql://" + this.getHost() + ":" + this.getMappedPort(MYSQL_PORT) + "/" + this.databaseName + additionalUrlParams;
    }

    protected String constructUrlForConnection(String queryString) {
        String url = super.constructUrlForConnection(queryString);
        if (!url.contains("useSSL=")) {
            String separator = url.contains("?") ? "&" : "?";
            url = url + separator + "useSSL=false";
        }

        if (!url.contains("allowPublicKeyRetrieval=")) {
            url = url + "&allowPublicKeyRetrieval=true";
        }

        return url;
    }

    public String getDatabaseName() {
        return this.databaseName;
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

    public SELF withConfigurationOverride(String s) {
        this.parameters.put("TC_MY_CNF", s);
        return this.self();
    }

    public SELF withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this.self();
    }

    public SELF withUsername(String username) {
        this.username = username;
        return this.self();
    }

    public SELF withPassword(String password) {
        this.password = password;
        return this.self();
    }

    static {
        IMAGE = DEFAULT_IMAGE_NAME.getUnversionedPart();
        MYSQL_PORT = 3306;
    }
}
