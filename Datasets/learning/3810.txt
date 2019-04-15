package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;

public class ColumnAdder
{
    /**
     * Add a column to the main query with given alias. Do nothing if something with given alias already present
     * (even if something else is in the columnName).
     *
     * @param ast        AST to modify (clone it if necessary)
     * @param tableName  tableName to look for
     * @param columnName column name to add to select list
     * @param alias      alias to use/search for
     * @throws IllegalArgumentException if given AST has no such table in the FROM field
     */
    public void addColumn(AstStart ast, String tableName, String columnName, String alias)
    {
        AstQuery query = ast.getQuery();
        AstSelect firstSelect = query.children().select(AstSelect.class).findFirst().get();
        if (firstSelect.getSelectList().children().select(AstDerivedColumn.class)
                .anyMatch(dc -> alias.equalsIgnoreCase(dc.getAlias())))
            return;
        query.children().select(AstSelect.class).forEach(select -> processSelect(select, tableName, columnName, alias));
    }

    private void processSelect(AstSelect select, String tableName, String columnName, String alias)
    {
        AstSelectList list = select.getSelectList();
        if(list.isAllColumns())
            throw new IllegalArgumentException("Cannot modify the 'SELECT *' statement");
        String tableAlias = getTableAlias(select.getFrom(), tableName);
        AstDerivedColumn col = new AstDerivedColumn(new AstFieldReference(tableAlias, columnName), alias).setSuffixComma(true)
                .setAsToken(true);
        list.addChild(col);
        list.moveToFront(col);
    }

    private String getTableAlias(AstFrom from, String tableName)
    {
        if (from == null)
            throw new IllegalArgumentException("Unable to add primary key: query does not contain the FROM clause");
        return from.getTableAlias(tableName).orElseThrow(
                () -> new IllegalArgumentException("FROM clause does not contain the table " + tableName));
    }
}
