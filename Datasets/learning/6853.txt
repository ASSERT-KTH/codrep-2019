package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.Token;

import java.util.Map;

public class OrderByFilter
{
    public void apply(AstStart start, Map<String, String> columns)
    {
        apply(start.getQuery(), columns);
    }

    public void apply(AstQuery query, Map<String, String> columns)
    {
        AstOrderBy orderBy = new AstOrderBy();
        apply(columns, query, orderBy);
        query.children().select(AstSelect.class).forEach(AstSelect::dropOrder);
        AstSelect select;
        if (query.jjtGetNumChildren() == 1)
            select = (AstSelect) query.child(0);
        else
        {
            AstTableRef tableRef = new AstTableRef(new AstParenthesis(query.clone()), new AstIdentifierConstant("tmp"));
            select = new AstSelect(new AstSelectList(), new AstFrom(tableRef));
            query.replaceWith(new AstQuery(select));
        }
        select.addChild(orderBy);
    }

    private void apply(Map<String, String> columns, AstQuery query, AstOrderBy orderBy)
    {
        for (Map.Entry<String, String> column : columns.entrySet())
        {
            int num = 0;
            for (AstDerivedColumn derColumn : query.tree().select(AstDerivedColumn.class))
            {
                if (column.getKey().equals(derColumn.getColumn()) || column.getKey().equals(derColumn.getAlias()))
                {
                    num = derColumn.jjtGetParent().indexOf(derColumn) + 1;
                    break;
                }
            }
            if (num == 0)
                throw new IllegalArgumentException("Unknown column " + column.getKey() + " in order clause");

            String dir = column.getValue();
            if (!dir.equalsIgnoreCase("ASC") && !dir.equalsIgnoreCase
("DESC"))
                throw new IllegalArgumentException("Unknown direction " + dir + ". Was expecting ASC or DESC");

            AstOrderingElement elem = new AstOrderingElement(0);
            elem.addChild(AstNumericConstant.of(num));
            elem.setDirectionToken(new Token(0, dir));
            orderBy.addChild(elem);
        }
    }
}
