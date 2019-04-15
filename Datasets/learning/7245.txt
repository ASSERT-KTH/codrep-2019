package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionService
{
    Connection getConnection() throws SQLException;

    Connection beginTransaction();

    void endTransaction();

    void rollbackTransaction();

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    void releaseConnection(java.sql.Connection conn);

    default RuntimeException returnRuntimeExceptionOrWrap(Throwable e)
    {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException("rethrow after rollback", e);
    
}
}
