package com.side.springtestbed.common.utils;

import com.side.springtestbed.common.exception.SQLStatementCountMismatchException;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;

public class SQLStatementCountValidator {

    private SQLStatementCountValidator() {}

    /**
     * Reset the statement recorder
     */
    public static void reset() {
        QueryCountHolder.clear();
    }

    /**
     * Assert select statement count
     *
     * @param expectedSelectCount expected select statement count
     */
    public static void assertSelectCount(int expectedSelectCount) {
        QueryCount queryCount = QueryCountHolder.getGrandTotal();
        long recordedSelectCount = queryCount.getSelect();
        if (expectedSelectCount != recordedSelectCount) {
            throw new SQLStatementCountMismatchException(expectedSelectCount, recordedSelectCount);
        }
    }

    /**
     * Assert insert statement count
     *
     * @param expectedInsertCount expected insert statement count
     */
    public static void assertInsertCount(int expectedInsertCount) {
        QueryCount queryCount = QueryCountHolder.getGrandTotal();
        long recordedInsertCount = queryCount.getInsert();
        if (expectedInsertCount != recordedInsertCount) {
            throw new SQLStatementCountMismatchException(expectedInsertCount, recordedInsertCount);
        }
    }

    /**
     * Assert update statement count
     *
     * @param expectedUpdateCount expected update statement count
     */
    public static void assertUpdateCount(int expectedUpdateCount) {
        QueryCount queryCount = QueryCountHolder.getGrandTotal();
        long recordedUpdateCount = queryCount.getUpdate();
        if (expectedUpdateCount != recordedUpdateCount) {
            throw new SQLStatementCountMismatchException(expectedUpdateCount, recordedUpdateCount);
        }
    }

    /**
     * Assert delete statement count
     *
     * @param expectedDeleteCount expected delete statement count
     */
    public static void assertDeleteCount(int expectedDeleteCount) {
        QueryCount queryCount = QueryCountHolder.getGrandTotal();
        long recordedDeleteCount = queryCount.getDelete();
        if (expectedDeleteCount != recordedDeleteCount) {
            throw new SQLStatementCountMismatchException(expectedDeleteCount, recordedDeleteCount);
        }
    }
}

