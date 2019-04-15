package com.developmentontheedge.be5.database.sql;

import java.sql.Connection;

@FunctionalInterface
public interface SqlExecutor<T>
{
    T run(Connection conn) throws Throwable ;
}
