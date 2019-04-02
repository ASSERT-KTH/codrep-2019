package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstBeSqlSubQuery;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;

import java.util.Arrays;
import java.util.List;

public class ColumnsApplier
{
    public void keepOnlyOutColumns(AstBeSqlSubQuery subQuery)
    {
        List<String> outColumns = Arrays.asList(subQuery.getOutColumns().split(","));

        AstSelect select = (AstSelect) subQuery.getQuery().child(0);
        AstSelectList selectList = select.getSelectList();
        if (selectList.isAllColumns())
        {
            throw new IllegalStateException("All columns not support " + selectList.getNodeContent());
        }
        else
        {
            for (int i = selectList.jjtGetNumChildren() - 1; i >= 	0; i--)
            {
                AstDerivedColumn derivedColumn = (AstDerivedColumn) selectList.jjtGetChild(i);
                if (!outColumns.contains(derivedColumn.getAlias()))
                {
                    derivedColumn.remove();
                }
            }

            if (selectList.jjtGetNumChildren() == 0)
            {
                throw new IllegalStateException("selectList is empty");
            }

            AstDerivedColumn lastColumn = (AstDerivedColumn) selectList.jjtGetChild(selectList.jjtGetNumChildren() - 1);
            lastColumn.setSuffixComma(false);

            AstDerivedColumn firstColumn = (AstDerivedColumn) selectList.jjtGetChild(0);
            firstColumn.removeSpecialPrefix();
        }
    }
}
