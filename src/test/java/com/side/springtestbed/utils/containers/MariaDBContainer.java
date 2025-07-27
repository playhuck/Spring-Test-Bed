package com.side.springtestbed.utils.containers;

import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.com.google.common.collect.Sets;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

public class MariaDBContainer<SELF extends MariaDBContainer<SELF>> extends JdbcDatabaseContainer<SELF> {
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("mariadb");
    /** @deprecated */
    @Deprecated
    public static final String DEFAULT_TAG = "10.3.6";
    public static final String NAME = "mariadb";
    /** @deprecated */
    @Deprecated
    public static final String IMAGE;
    static final String DEFAULT_USER = "test";
    static final String DEFAULT_PASSWORD = "test";
    static final Integer MARIADB_PORT;
    private String databaseName;
    private String username;
    private String password;
    private static final String MARIADB_ROOT_USER = "root";
    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";

    /** @deprecated */
    @Deprecated
    public MariaDBContainer() {
        this(DEFAULT_IMAGE_NAME.withTag("10.3.6"));
    }

    public MariaDBContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public MariaDBContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.databaseName = "test";
        this.username = "test";
        this.password = "test";
        dockerImageName.assertCompatibleWith(new DockerImageName[]{DEFAULT_IMAGE_NAME});
        this.addExposedPort(MARIADB_PORT);
    }

    public Set<Integer> getLivenessCheckPortNumbers() {
        return Sets.newHashSet(new Integer[]{MARIADB_PORT});
    }

    protected void configure() {
        this.optionallyMapResourceParameterAsVolume("TC_MY_CNF", "/etc/mysql/conf.d", "mariadb-default-conf", 16877);
        this.addEnv("MYSQL_DATABASE", this.databaseName);
        this.addEnv("MYSQL_USER", this.username);
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
        return "org.mariadb.jdbc.Driver";
    }

    public String getJdbcUrl() {
        String additionalUrlParams = this.constructUrlParameters("?", "&");
        return "jdbc:mariadb://" + this.getHost() + ":" + this.getMappedPort(MARIADB_PORT) + "/" + this.databaseName + additionalUrlParams;
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
        return (SELF) this.self();
    }

    public SELF withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return (SELF) this.self();
    }

    public SELF withUsername(String username) {
        this.username = username;
        return (SELF) this.self();
    }

    public SELF withPassword(String password) {
        this.password = password;
        return (SELF) this.self();
    }

    static {
        IMAGE = DEFAULT_IMAGE_NAME.getUnversionedPart();
        MARIADB_PORT = 3306;
    }
}

