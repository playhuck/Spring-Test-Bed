package com.side.springtestbed.utils.utils;

import org.hsqldb.HsqlException;
import org.hsqldb.error.Error;
import org.hsqldb.result.Result;

import java.sql.*;

public final class JDBCUtil {
    public JDBCUtil() {
    }

    public static SQLException sqlException(HsqlException var0) {
        return sqlException(var0.getMessage(), var0.getSQLState(), var0.getErrorCode(), var0);
    }

    public static SQLException sqlException(HsqlException var0, Throwable var1) {
        return sqlException(var0.getMessage(), var0.getSQLState(), var0.getErrorCode(), var1);
    }

    public static SQLException sqlException(int var0) {
        return sqlException(org.hsqldb.error.Error.error(var0));
    }

    public static SQLException sqlExceptionSQL(int var0) {
        return sqlException(Error.error(var0));
    }

    public static SQLException sqlException(int var0, String var1) {
        return sqlException(Error.error(var0, var1));
    }

    public static SQLException sqlException(int var0, String var1, Throwable var2) {
        return sqlException(Error.error(var0, var1), var2);
    }

    public static SQLException sqlException(int var0, int var1) {
        return sqlException(Error.error(var0, var1));
    }

    static SQLException sqlException(int var0, int var1, String[] var2) {
        return sqlException(Error.error((Throwable)null, var0, var1, var2));
    }

    public static SQLException notSupported() {
        HsqlException var0 = Error.error(1500);
        return new SQLFeatureNotSupportedException(var0.getMessage(), var0.getSQLState(), -1500);
    }

    static SQLException notUpdatableColumn() {
        return sqlException(2500);
    }

    public static SQLException nullArgument() {
        return sqlException(423);
    }

    public static SQLException nullArgument(String var0) {
        return sqlException(423, var0 + ": null");
    }

    public static SQLException invalidArgument() {
        return sqlException(423);
    }

    public static SQLException invalidArgument(String var0) {
        return sqlException(423, var0);
    }

    public static SQLException invalidArgument(int var0) {
        String var1 = Error.getMessage(var0);
        return sqlException(423, var1);
    }

    public static SQLException outOfRangeArgument() {
        return sqlException(423);
    }

    public static SQLException outOfRangeArgument(String var0) {
        return sqlException(423, var0);
    }

    public static SQLException connectionClosedException() {
        return sqlException(1303);
    }

    public static SQLWarning sqlWarning(Result var0) {
        return new SQLWarning(var0.getMainString(), var0.getSubString(), var0.getErrorCode());
    }

    public static SQLException sqlException(Throwable var0) {
        return new SQLException(var0);
    }

    public static SQLException sqlException(Result var0) {
        return sqlException(var0.getMainString(), var0.getSubString(), var0.getErrorCode(), var0.getException());
    }

    public static SQLException sqlException(String var0, String var1, int var2, Throwable var3) {
        if (var1.startsWith("08")) {
            return (SQLException)(!var1.endsWith("3") ? new SQLTransientConnectionException(var0, var1, var2, var3) : new SQLNonTransientConnectionException(var0, var1, var2, var3));
        } else if (var1.startsWith("22")) {
            return new SQLDataException(var0, var1, var2, var3);
        } else if (var1.startsWith("23")) {
            return new SQLIntegrityConstraintViolationException(var0, var1, var2, var3);
        } else if (var1.startsWith("28")) {
            return new SQLInvalidAuthorizationSpecException(var0, var1, var2, var3);
        } else if (!var1.startsWith("42") && !var1.startsWith("37") && !var1.startsWith("2A")) {
            if (var1.startsWith("40")) {
                return new SQLTransactionRollbackException(var0, var1, var2, var3);
            } else {
                return (SQLException)(var1.startsWith("0A") ? new SQLFeatureNotSupportedException(var0, var1, var2, var3) : new SQLException(var0, var1, var2, var3));
            }
        } else {
            return new SQLSyntaxErrorException(var0, var1, var2, var3);
        }
    }
}

