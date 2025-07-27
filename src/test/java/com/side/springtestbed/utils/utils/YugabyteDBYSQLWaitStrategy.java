package com.side.springtestbed.utils.utils;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public final class YugabyteDBYSQLWaitStrategy extends AbstractWaitStrategy {
    private static final Logger log = LoggerFactory.getLogger(YugabyteDBYSQLWaitStrategy.class);
    private static final String YSQL_TEST_QUERY = "SELECT 1";
    private final WaitStrategyTarget target;

    public void waitUntilReady(WaitStrategyTarget target) {
        YugabyteDBYSQLContainer container = (YugabyteDBYSQLContainer)target;
        Unreliables.retryUntilSuccess((int)this.startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
            this.getRateLimiter().doWhenReady(() -> {
                try {
                    Connection con = container.createConnection(container.getJdbcUrl());
                    Throwable var2 = null;

                    try {
                        con.createStatement().execute("SELECT 1");
                    } catch (Throwable var12) {
                        var2 = var12;
                        throw var12;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var11) {
                                    var2.addSuppressed(var11);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (SQLException var14) {
                    SQLException ex = var14;
                    log.error("Error connecting to the database", ex);
                }

            });
            return true;
        });
    }

    public void waitUntilReady() {
        this.waitUntilReady(this.target);
    }

    public YugabyteDBYSQLWaitStrategy(WaitStrategyTarget target) {
        this.target = target;
    }
}
