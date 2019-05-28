package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.format.dbms.Context;
import com.developmentontheedge.sql.format.dbms.DbmsTransformer;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbServiceImpl implements DbService
{
    private static final Logger log = Logger.getLogger(DbServiceImpl.class.getName());

    private final Cache<String, String> formatSqlCache;

    private QueryRunner queryRunner;
    private ConnectionService connectionService;
    private DbmsTransformer dbmsTransformer;

    @Inject
    public DbServiceImpl(ConnectionService connectionService, DataSourceService databaseService, Be5Caches be5Caches)
    {
        this.connectionService = connectionService;
        queryRunner = new QueryRunner();
        formatSqlCache = be5Caches.createCache("Format sql");
        Context context = new Context(databaseService.getDbms());
        this.dbmsTransformer = context.getDbmsTransformer();
        dbmsTransformer.setParserContext(DefaultParserContext.getInstance());
    }

    @Override
    public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
    {
        return execute(conn -> query(conn, sql, rsh, params));
    }

    @Override
    public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
    {
        return execute(conn -> query(conn, sql, rs -> rs.next() ? parser.parse(rs) : null, params));
    }

    @Override
    public <T> List<T> list(String sql, ResultSetParser<T> parser, Object... params)
    {
        return execute(conn -> query(conn, sql, rs -> listWrapper(rs, parser), params));
    }

    @Override
    public <T> List<T> list(AstStart astStart, ResultSetParser<T> parser, Object... params)
    {
        return execute(conn -> query(conn, astStart, rs -> listWrapper(rs, parser), params));
    }

    private <T> List<T> listWrapper(ResultSet rs, ResultSetParser<T> parser) throws SQLException
    {
        List<T> rows = new ArrayList<>();
        while (rs.next())
        {
            rows.add(parser.parse(rs));
        }
        return rows;
    }

    @Override
    public <T> T one(String sql, Object... params)
    {
        return execute(conn -> query(conn, sql, new ScalarHandler<>(), params));
    }

    @Override
    public int update(String sql, Object... params)
    {
        return execute(conn -> update(conn, sql, params));
    }

    @Override
    public int updateUnsafe(String sql, Object... params)
    {
        return execute(conn -> updateUnsafe(conn, sql, params));
    }

    @Override
    public <T> T insert(String sql, Object... params)
    {
        return execute(conn -> insert(conn, sql, params));
    }

    @Override
    public String format(String sql)
    {
        return formatSqlCache.get(sql, k -> {
            AstStart astStart = SqlQuery.parse(k);
            new MacroExpander().expandMacros(astStart);
            return format(astStart);
        });
    }

    @Override
    public String format(AstStart astStart)
    {
        dbmsTransformer.transformAst(astStart);
        return astStart.format();
    }

    private <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.query(conn, sql, rsh, params);
    }

    private <T> T query(Connection conn, AstStart astStart, ResultSetHandler<T> rsh, Object... params) throws SQLException
    {
        String sql = format(astStart);
        log.fine
(sql + Arrays.toString(params));
        return queryRunner.query(conn, sql, rsh, params);
    }

    private int update(Connection conn, String sql, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private int updateUnsafe(Connection conn, String sql, Object... params) throws SQLException
    {
        log.warning("Unsafe update (not be-sql parsed and formatted): " + sql + Arrays.toString(params));
        return queryRunner.update(conn, sql, params);
    }

    private <T> T insert(Connection conn, String sql, Object... params) throws SQLException
    {
        sql = format(sql);
        log.fine(sql + Arrays.toString(params));
        return queryRunner.insert(conn, sql, new ScalarHandler<>(), params);
    }

    @Override
    public <T> T execute(SqlExecutor<T> executor)
    {
        Connection conn = null;
        try
        {
            conn = connectionService.getConnection();
            return executor.run(conn);
        }
        catch (RuntimeException e)
        {
            log.log(Level.SEVERE, "", e);
            throw e;
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "", e);
            throw new RuntimeException(e);
        }
        finally
        {
            connectionService.releaseConnection(conn);
        }
    }

    @Override
    public <T> T transactionWithResult(SqlExecutor<T> executor)
    {
        return connectionService.transactionWithResult(executor);
    }

    @Override
    public void transaction(SqlExecutorVoid executor)
    {
        connectionService.transaction(executor);
    }
}
