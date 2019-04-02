package com.developmentontheedge.be5.database.sql.parsers;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.ResultSetWrapper;
import com.developmentontheedge.be5.database.util.SqlUtils;

import java.sql.SQLException;


public class ScalarLongParser implements ResultSetParser<Long>
{
    private final int columnIndex;
    private final String columnName;

    public ScalarLongParser()
    {
        this(1, null);
    }

    public ScalarLongParser(final int columnIndex)
    {
        this( columnIndex, null);
    }

    public ScalarLongParser(final String columnName)
    {
        this(1, columnName);
    }

    private ScalarLongParser(final int columnIndex, final String columnName)
    {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long parse(final ResultSetWrapper rs) throws SQLException
    {
        if (this.columnName == null)
        {
            return SqlUtils.longFromDbObject(rs.getObject(this.columnIndex));
        }
        return SqlUtils.longFromDbObject(rs.getObject(this.columnName));
    }
}
