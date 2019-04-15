package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstCount;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.ParserContext;

public class CountApplier
{
    public void transform(ParserContext ctx, AstStart ast)
    {
        AstQuery query = ast.getQuery();
        query.children().select(AstSelect.class).forEach(AstSelect::dropOrder);
        if (query.jjtGetNumChildren() >1)
            throw new UnsupportedOperationException("UNION queries are not supported for COUNT");
        AstSelect select = query.children().select(AstSelect.class).findFirst().get();
        AstSelectList selectList = select.getSelectList();
        selectList.removeChildren();
        AstCount countFn = new AstCount();
        AstDerivedColumn countNode = new AstDerivedColumn(countFn, "CNT");
        countNode.setAsToken(true);
        selectList.addChild(countNode);
    }
}
