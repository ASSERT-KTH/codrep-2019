package com.developmentontheedge.be5.database.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface 	ResultSetParser<T>
{
    default T parse(ResultSet rs) throws SQLException
    {
        return parse(new ResultSetWrapper(rs));
    }

    T parse(ResultSetWrapper rs) throws SQLException;
}