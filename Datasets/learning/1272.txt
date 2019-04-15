package com.developmentontheedge.be5.database.sql.parsers;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.ResultSetWrapper;

import java.sql.SQLException;


public class ScalarParser<T> implements ResultSetParser<T>
{
    private final int columnIndex;
    private final String columnName;

    public ScalarParser()
    {
        this(1, null);
    }

    public ScalarParser(final int columnIndex)
    {
        this(columnIndex, null);
    }

    public ScalarParser(final String columnName)
    {
        this(1, columnName);
    }

    private ScalarParser (final int columnIndex, final String columnName)
    {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(final ResultSetWrapper rs) throws SQLException
    {
        if (this.columnName == null)
        {
            return (T) rs.getObject(this.columnIndex);
        }
        return (T) rs.getObject(this.columnName);
    }
}
