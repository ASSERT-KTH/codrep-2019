package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.SimpleNode;

public class ColumnRef
{
    private final String table;
    private final String name;

    ColumnRef(String table, String name)
    {
        this.table = table;
        this.name = name;
    }

    public SimpleNode asNode()
    {
        return table == null ? new AstIdentifierConstant(name) : new AstFieldReference(table, name);
    }

    public static ColumnRef resolve(AstStart ast, String value)
    {
        return resolve(ast.getQuery(), value);
    }

    /*
    TODO implement resolve with checks in meta (in query module)
    --
    select role_name from users
    inner join user_roles ON user_roles.user_name = users.user_name
    */
    public static ColumnRef resolve(AstQuery query, String column)
    {
        String[] parts = column.split("[.]");
        if (parts.length > 3) throw new IllegalArgumentException("");

        for (AstSelect select : query.children().select(AstSelect.class))
        {
            for (AstDerivedColumn derColumn : select.tree().select(AstDerivedColumn.class))
            {
                String queryColumn = derColumn.getAlias() != null ? derColumn.getAlias() : derColumn.getColumn();
                if (column.equals(queryColumn))
                    return new ColumnRef(null, column);
            }
        }

        String columnTableName, columnName;
        if (parts.length == 2)
        {
            columnTableName = parts[0];
            columnName = parts[1];
        }
        else
        {
            columnTableName = parts[0] + "." + parts[1];
            columnName = parts[2];
        }

        for (AstSelect select : query.children().select(AstSelect.class))
        {
            for (AstTableRef table : select.tree().select(AstTableRef.class))
            {
                String tableName = table.getAlias() != null ? table.getAlias() : table.getTable();
                if (columnTableName.equals(tableName) || columnTableName.equals(table.getTable()))
                {
                    return new ColumnRef(tableName, columnName);
                }
            }
        }
        return null;
    }

    
public String getTable()
    {
        return table;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnRef columnRef = (ColumnRef) o;

        if (table != null ? !table.equals(columnRef.table) : columnRef.table != null) return false;
        return name != null ? name.equals(columnRef.name) : columnRef.name == null;
    }

    @Override
    public int hashCode()
    {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "ColumnRef{" +
                "table='" + table + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
