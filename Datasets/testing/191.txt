package com.developmentontheedge.sql.model;

import java.util.Iterator;
import java.util.Map;


public class AstWhere extends SimpleNode
{
    public static final String NOT_NULL = "NOT_NULL";

    public static AstWhere withReplacementParameter(String columnName, int count)
    {
        AstWhere astWhere = new AstWhere(SqlParserTreeConstants.JJTWHERE);
        astWhere.addChild(AstInPredicate.withReplacementParameter(columnName, count));

        return astWhere;
    }

    public AstWhere(Map<String, ?> conditions)
    {
        this(SqlParserTreeConstants.JJTWHERE);
        if (conditions.size() > 0)
        {
            Iterator<? extends Map.Entry<String, ?>> iterator = conditions.entrySet().iterator();
            iterator.hasNext();
            addChild(addAstFunNode(iterator));
        }
    }

    private SimpleNode addAstFunNode(Iterator<? extends Map.Entry<String, ?>> iterator)
    {
//        TODO add !=, NOT LIKE
//                externalStatus: "!=ok"

        Map.Entry<String, ?> entry = iterator.next();
        Object valueObj = entry.getValue();
        PredefinedFunction function = DefaultParserContext.FUNC_EQ;
        SimpleNode astFunNode = function.node(new AstFieldReference(entry.getKey()), AstReplacementParameter.get());

        if ( valueObj == null)
        {
            astFunNode = new AstNullPredicate(true, new AstFieldReference(entry.getKey()));
        }
        else if (valueObj.getClass().isArray())
        {
            function = DefaultParserContext.FUNC_IN;

            int len;
            if (valueObj.getClass() == int[].class)
            {
                len = ((int[]) valueObj).length;
            }
            else if (valueObj.getClass() == long[].class)
            {
                len = ((long[]) valueObj).length;
            }
            else if (valueObj.getClass() == short[].class)
            {
                len = ((short[]) valueObj).length;
            }
            else if (valueObj.getClass() == char[].class)
            {
                len = ((char[]) valueObj).length;
            }
            else if (valueObj.getClass() == byte[].class)
            {
                len = ((byte[]) valueObj).length;
            }
            else if (valueObj.getClass() == float[].class)
            {
                len = ((float[]) valueObj).length;
            }
            else if (valueObj.getClass() == double[].class)
            {
                len = ((double[]) valueObj).length;
            }
            else
            {
                len = ((Object[]) valueObj).length;
            }

            astFunNode = function.node(new AstFieldReference(entry.getKey()),
                    AstInValueList.withReplacementParameter(len));
        }
        else if (valueObj instanceof String)
        {
            String value = (String) valueObj;
            if (value.equals(NOT_NULL))
            {
                astFunNode = new AstNullPredicate(false, new AstFieldReference(entry.getKey()));
            }
            else if (value.endsWith("%") || value.startsWith("%"))
            {
                function = DefaultParserContext.FUNC_LIKE;
                astFunNode = function.node(new AstFieldReference(entry.getKey()), AstReplacementParameter.get());
            }
        }

        if (iterator.hasNext())
        {
            return new AstBooleanTerm(astFunNode, addAstFunNode(iterator));
        }
        else
        {
            return astFunNode;
        }
    }

    public AstWhere(int id)
    {
        super(id);
        this.nodePrefix = "WHERE";
    }

    public AstWhere()
    {
        this(SqlParserTreeConstants.JJTWHERE);
    }
}
