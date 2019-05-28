package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstTableRef;

public class LimitsApplier
{
    int offset, count;

    public LimitsApplier(int offset, int count)
    {
        super();
        this.offset = offset;
        this.count = count;
    }

    public boolean transform(AstStart ast)
    {
        AstQuery query = ast.getQuery();
        return transformQuery(query);
    }

    public boolean transformQuery(AstQuery query)
    {
        if (query.jjtGetNumChildren() == 1)        {
            AstSelect select = (AstSelect) query.child(0);
            if (select.getLimit() != null)
                return false;
            AstLimit limit = new AstLimit();
            limit.setLimit(offset, count);
            select.addChild(limit);
        }
        else
        {
            AstTableRef tableRef = new AstTableRef(new AstParenthesis(query.clone()), new AstIdentifierConstant("tmp"));
            AstSelect select = new AstSelect(new AstSelectList(), new AstFrom(tableRef));
            AstLimit limit = new AstLimit();
            limit.setLimit(offset, count);
            select.addChild(limit);
            query.replaceWith(new AstQuery(select));
        }
        // TODO: support offset, union, etc.
        return true;
    }
}
