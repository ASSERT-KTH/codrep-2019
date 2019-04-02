package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.SqlExecutor;
import com.developmentontheedge.be5.database.sql.SqlExecutorVoid;
import com.developmentontheedge.be5.database.sql.parsers.ScalarLongParser;
import com.developmentontheedge.be5.database.sql.parsers.ScalarParser;
import com.developmentontheedge.be5.database.util.SqlUtils;
import com.developmentontheedge.sql.model.AstStart;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.annotation.Nullable;
import java.util.List;


public interface DbService
{
    @Nullable
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    @Nullable
    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> list(String sql,ResultSetParser<T> parser, Object... params);

    <T> List<T> list(AstStart astStart, ResultSetParser<T> parser, Object... params);

    @Nullable
    <T> T one(String sql, Object... params);

    int update(String sql, Object... params);

    int updateUnsafe(String sql, Object... params);

    <T> T insert(String sql, Object... params);

    String format(String sql);

    String format(AstStart astStart);

    <T> T execute(SqlExecutor<T> executor);

    <T> T transactionWithResult(SqlExecutor<T> executor);

    void transaction(SqlExecutorVoid executor);

    @Nullable
    default Long oneLong(String sql, Object... params)
    {
        return SqlUtils.longFromDbObject(one(sql, params));
    }

    default long countFrom(String sql, Object... params)
    {
        if (!(sql.startsWith("SELECT COUNT(1) FROM ") || sql.startsWith("SELECT count(1) FROM ") ||
                sql.startsWith("SELECT COUNT(*) FROM ") || sql.startsWith("SELECT count(*) FROM ") ||
                sql.startsWith("SELECT COUNT(*) AS \"count\" FROM ")))
        {
            sql = "SELECT COUNT(1) FROM " + sql;
        }
        return SqlUtils.longFromDbObject(one(sql, params));
    }

    @Nullable
    default String oneString(String sql, Object... params)
    {
        return SqlUtils.stringFromDbObject(one(sql, params));
    }

    @Nullable
    default Integer oneInteger(String sql, Object... params)
    {
        return one(sql, params);
    }

    default <T> List<T> scalarList(String sql, Object... params)
    {
        return list(sql, new ScalarParser<T>(), params);
    }

    default List<Long> scalarLongList(String sql, Object... params)
    {
        return list(sql, new ScalarLongParser(), params);
    }

    default Long[] longArray(String sql, Object... params)
    {
        return scalarLongList(sql, params).toArray(new Long[0]);
    }

    default String[] stringArray(String sql, Object... params)
    {
        return list(sql, new ScalarParser<String>(), params).toArray(new String[0]);
    }

}
