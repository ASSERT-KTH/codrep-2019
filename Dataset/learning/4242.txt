package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql. model.AstSelect;
import com.developmentontheedge.sql.model.AstUnion;
import com.developmentontheedge.sql.model.SetQuantifier;

public class DistinctApplier
{
    public void transformQuery(AstQuery query)
    {
        query.tree().select(AstSelect.class).forEach(node -> node.setQuantifier(SetQuantifier.DISTINCT));
        query.tree().select(AstUnion.class).forEach(node -> node.setQuantifier(SetQuantifier.DISTINCT));
    }
}
