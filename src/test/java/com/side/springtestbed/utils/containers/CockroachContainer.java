package com.side.springtestbed.utils.containers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.ComparableVersion;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class CockroachContainer extends JdbcDatabaseContainer<CockroachContainer> {
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("cockroachdb/cockroach");
    private static final String DEFAULT_TAG = "v19.2.11";
    public static final String NAME = "cockroach";
    /** @deprecated */
    @Deprecated
    public static final String IMAGE;
    /** @deprecated */
    @Deprecated
    public static final String IMAGE_TAG = "v19.2.11";
    private static final String JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String JDBC_URL_PREFIX = "jdbc:postgresql";
    private static final String TEST_QUERY_STRING = "SELECT 1";
    private static final int REST_API_PORT = 8080;
    private static final int DB_PORT = 26257;
    private static final String FIRST_VERSION_WITH_ENV_VARS_SUPPORT = "22.1.0";
    private String databaseName;
    private String username;
    private String password;
    private boolean isVersionGreaterThanOrEqualTo221;

    /** @deprecated */
    @Deprecated
    public CockroachContainer() {
        this(DEFAULT_IMAGE_NAME.withTag("v19.2.11"));
    }

    public CockroachContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public CockroachContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.databaseName = "postgres";
        this.username = "root";
        this.password = "";
        dockerImageName.assertCompatibleWith(new DockerImageName[]{DEFAULT_IMAGE_NAME});
        this.isVersionGreaterThanOrEqualTo221 = this.isVersionGreaterThanOrEqualTo221(dockerImageName);
        this.withExposedPorts(new Integer[]{8080, 26257});
        this.waitingFor((new HttpWaitStrategy()).forPath("/health").forPort(8080).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(1L)));
        this.withCommand("start-single-node --insecure");
    }

    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    public String getJdbcUrl() {
        String additionalUrlParams = this.constructUrlParameters("?", "&");
        return "jdbc:postgresql://" + this.getHost() + ":" + this.getMappedPort(26257) + "/" + this.databaseName + additionalUrlParams;
    }

    public final String getDatabaseName() {
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

    public CockroachContainer withUsername(String username) {
        this.validateIfVersionSupportsUsernameOrPasswordOrDatabase("username");
        this.username = username;
        return (CockroachContainer)this.withEnv("COCKROACH_USER", username);
    }

    public CockroachContainer withPassword(String password) {
        this.validateIfVersionSupportsUsernameOrPasswordOrDatabase("password");
        this.password = password;
        return (CockroachContainer)((CockroachContainer)this.withEnv("COCKROACH_PASSWORD", password)).withCommand("start-single-node");
    }

    public CockroachContainer withDatabaseName(String databaseName) {
        this.validateIfVersionSupportsUsernameOrPasswordOrDatabase("databaseName");
        this.databaseName = databaseName;
        return (CockroachContainer)this.withEnv("COCKROACH_DATABASE", databaseName);
    }

    private boolean isVersionGreaterThanOrEqualTo221(DockerImageName dockerImageName) {
        ComparableVersion version = new ComparableVersion(dockerImageName.getVersionPart().replaceFirst("v", ""));
        return version.isGreaterThanOrEqualTo("22.1.0");
    }

    private void validateIfVersionSupportsUsernameOrPasswordOrDatabase(String parameter) {
        if (!this.isVersionGreaterThanOrEqualTo221) {
            throw new UnsupportedOperationException(String.format("Setting a %s in not supported in the versions below 22.1.0", parameter));
        }
    }

    static {
        IMAGE = DEFAULT_IMAGE_NAME.getUnversionedPart();
    }
}

