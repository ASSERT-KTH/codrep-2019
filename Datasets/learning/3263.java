package com.developmentontheedge.be5.database.sql;

import java. sql.Connection;

@FunctionalInterface
public interface SqlExecutorVoid
{
    void run(Connection conn) throws Throwable;
}
