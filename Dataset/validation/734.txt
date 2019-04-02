package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionServiceImpl implements ConnectionService
{
    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TRANSACT_CONN_COUNT = new ThreadLocal<>();

    private static final Logger log = Logger.getLogger(ConnectionServiceImpl.class.getName());

    private DataSourceService databaseService;

    @Inject
    public ConnectionServiceImpl(DataSourceService databaseService)
    {
        this.databaseService = databaseService;
    }

    Connection getCurrentTxConn()
    {
        return TRANSACT_CONN.get();
    }

    @Override
    public Connection beginTransaction()
    {
        Connection txConnection = getCurrentTxConn();
        if (txConnection == null)
        {
            TRANSACT_CONN_COUNT.set(1);
            return beginWorkWithTxConnection();
        }
        else
        {
            TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() + 1);
            return txConnection;
        }
    }

    @Override
    public void endTransaction()
    {
        TRANSACT_CONN_COUNT.set(TRANSACT_CONN_COUNT.get() - 1);
        Connection txConnection = getCurrentTxConn();
        if (txConnection != null && TRANSACT_CONN_COUNT.get() == 0)
        {
            try
            {
                txConnection.commit();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                endWorkWithTxConnection();
            }
        }
    }

    @Override
    public void rollbackTransaction()
    {
        Connection txConnection = getCurrentTxConn();
        try
        {
            if (txConnection != null && !txConnection.isClosed())
            {
                txConnection.rollback();
            }
        }
        catch (SQLException e)
        {
            log.log(Level.SEVERE, "Unable to rollback transaction", e);
            throw new RuntimeException(e);
        }
        finally
        {
            endWorkWithTxConnection();
        }
    }

    private Connection beginWorkWithTxConnection()
    {
        try
        {
            Connection conn = databaseService.getDataSource()	.getConnection();
            conn.setAutoCommit(false);
            TRANSACT_CONN.set(conn);
            return conn;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void endWorkWithTxConnection()
    {
        returnConnection(getCurrentTxConn());
        TRANSACT_CONN.set(null);
    }

    @Override
    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        Connection conn;
        try
        {
            conn = beginTransaction();
            T res = executor.run(conn);
            endTransaction();
            return res;
        }
        catch (Throwable e)
        {
            rollbackTransaction();
            throw returnRuntimeExceptionOrWrap(e);
        }
    }

    @Override
    public void transaction(SqlExecutorVoid executor)
    {
        transactionWithResult(getWrapperExecutor(executor));
    }

    private static SqlExecutor<Void> getWrapperExecutor(final SqlExecutorVoid voidExecutor)
    {
        return conn -> {
            voidExecutor.run(conn);
            return null;
        };
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        if (isInTransaction())
        {
            return getCurrentTxConn();
        }
        else
        {
            return databaseService.getDataSource().getConnection();
        }
    }

    @Override
    public void releaseConnection(Connection conn)
    {
        if (!isInTransaction())
        {
            returnConnection(conn);
        }
    }

    private void returnConnection(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                if (!conn.isClosed())
                {
                    if (!conn.getAutoCommit())
                        conn.setAutoCommit(true);
                    if (conn.isReadOnly())
                        conn.setReadOnly(false);

                    conn.close();
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isInTransaction()
    {
        return getCurrentTxConn() != null;
    }

}
