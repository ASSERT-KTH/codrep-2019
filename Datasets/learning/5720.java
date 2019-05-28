package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstConcatExpression;
import com.developmentontheedge.sql.model.AstExcept;
import com.developmentontheedge.sql.model.AstExtract;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderedSetAggregate;
import com.developmentontheedge.sql.model.AstOrderingElement;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstPosition;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstStringPart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.AstWhere;
import com.developmentontheedge.sql.model.AstWith;
import com.developmentontheedge.sql.model.AstWithinGroup;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.Function;
import com.developmentontheedge.sql.model.PredefinedFunction;
import com.developmentontheedge.sql.model.SimpleNode;
import com.developmentontheedge.sql.model.SqlParserTreeConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleTransformer extends GenericDbmsTransformer
{
    private static final PredefinedFunction NVL = new PredefinedFunction("NVL", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction MONTHS_BETWEEN = new PredefinedFunction("MONTHS_BETWEEN", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction BITAND = new PredefinedFunction("BITAND", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction LISTAGG = new PredefinedFunction("LISTAGG", PredefinedFunction.AGGREGATE_FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction EDIT_DISTANCE = new PredefinedFunction("UTL_MATCH.EDIT_DISTANCE", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction TO_NUMBER = new PredefinedFunction("TO_NUMBER", PredefinedFunction.FUNCTION_PRIORITY, 2);

    @Override
    protected void transformExcept(AstExcept node)
    {
        node.setMinus();
    }

    @Override
    protected void transformLevenshtein(AstFunNode node)
    {
        node.setFunction(EDIT_DISTANCE);
    }

    @Override
    protected void transformRegexpLike(AstFunNode node)
    {
        if ("~".equals(node.getFunction().getName()))
            node.setFunction(parserContext.getFunction("regexp_like"));
    }

    @Override
    protected void transformStringAgg(AstFunNode node)
    {
        node.setFunction(LISTAGG);
        if (node.isDistinct())
            throw new IllegalStateException("DISTINCT clause is unsupported for " + node.getFunction().getName());
        AstOrderBy orderBy;
        if (node.child(node.jjtGetNumChildren() - 1) instanceof AstOrderBy)
        {
            orderBy = (AstOrderBy) node.child(node.jjtGetNumChildren() - 1);
            node.removeChild(node.jjtGetNumChildren() - 1);
        }
        else
        {
            orderBy = new AstOrderBy(new AstOrderingElement(node.child(0)));
        }
        node.replaceWith(new AstOrderedSetAggregate((AstFunNode) node.clone(), new AstWithinGroup(orderBy)));
    }

    @Override
    protected void transformRight(AstFunNode node)
    {
        node.replaceWith(parserContext.getFunction("substr").node(node.child(0), DefaultParserContext.FUNC_UMINUS.node(node.child(1))));
    }

    @Override
    protected void transformLeft(AstFunNode node)
    {
        node.replaceWith(parserContext.getFunction("substr").node(node.child(0), AstNumericConstant.of(1), node.child(1)));
    }

    @Override
    protected void transformWith(AstWith with)
    {
        with.setRecursion(false);
    }

    @Override
    protected void transformBitAnd(AstFunNode node)
    {
        node.setFunction(BITAND);
    }

    @Override
    protected void transformBitOr(AstFunNode node)
    {
        AstFunNode sum = DefaultParserContext.FUNC_PLUS.node(node.child(0), node.child(1));
        node.replaceWith(DefaultParserContext.FUNC_MINUS.node(sum, BITAND.node(node.child(0), node.child(1))));
    }

    @Override
    protected void transformLastDayPostgres(SimpleNode node, SimpleNode date)
    {
        node.replaceWith(parserContext.getFunction("last_day").node(date));
    }

    @Override
    protected SimpleNode getDateTimeDiff(SimpleNode startDate, SimpleNode endDate, String format)
    {
        Function trunc = parserContext.getFunction("trunc");
        AstParenthesis dateDifference = new AstParenthesis(DefaultParserContext.FUNC_MINUS.node(new AstCast(endDate, "DATE"), new AstCast(startDate, "DATE")));
        PredefinedFunction opTimes = DefaultParserContext.FUNC_TIMES;
        switch (format)
        {
            case "SECOND":
                return trunc.node(opTimes.node(dateDifference, AstNumericConstant.of(24 * 60 * 60)));
            case "MINUTE":
                return trunc.node(opTimes.node(dateDifference, AstNumericConstant.of(24 * 60)));
            case "HOUR":
                return trunc.node(opTimes.node(dateDifference, AstNumericConstant.of(24)));
            case "DAY":
                return trunc.node(dateDifference);
            case "MONTH":
                return MONTHS_BETWEEN.node(trunc.node(endDate, new AstStringConstant(format)), trunc.node(startDate, new AstStringConstant(format)));
            case "YEAR":
                PredefinedFunction opDivide = DefaultParserContext.FUNC_DIVIDE;
                return parserContext.getFunction("floor").node(opDivide.node(MONTHS_BETWEEN.node(endDate, startDate), AstNumericConstant.of(12)));
            default:
                throw new IllegalStateException("Unsupported value for datepart in TIMESTAMPDIFF: " + format);
        }
    }

    @Override
    protected void transformDateAdd(AstFunNode node)
    {
        SimpleNode date = node.child(0);
        SimpleNode number = node.child(1);
        String name = node.getFunction().getName();
        if (name.equalsIgnoreCase("add_days"))
            node.replaceWith(
                    new AstParenthesis(DefaultParserContext.FUNC_PLUS.node(new AstParenthesis(date), new AstParenthesis(number))));
        if (name.equalsIgnoreCase("add_millis"))
            node.replaceWith(new AstParenthesis(DefaultParserContext.FUNC_PLUS.node(date,
                    new AstParenthesis(DefaultParserContext.FUNC_DIVIDE.node(number, AstNumericConstant.of(86400000))))));
    }

    @Override
    protected void transformPosition(AstPosition position)
    {
        position.replaceWith(parserContext.getFunction("instr").node(position.child(1), position.child(0)));
    }

    @Override
    protected void transformDateTrunc(AstFunNode node)
    {
        node.replaceWith(parserContext.getFunction("trunc").node(node.child(1), node.child(0)));
    }

    @Override
    protected void transformExtract(AstExtract extract)
    {
        transformYearMonthDay(extract);
    }

    @Override
    protected void transformYearMonthDay(SimpleNode node)
    {
        String dateField = "";
        String name = node instanceof AstFunNode ? ((AstFunNode) node).getFunction().getName() : ((AstExtract) node).getDateField();
        if ("year".equalsIgnoreCase(name))
            dateField = "YYYY";
        if ("month".equalsIgnoreCase(name))
            dateField = "MM";
        if ("day".equalsIgnoreCase(name))
            dateField = "DD";

        node.replaceWith(parserContext.getFunction("to_number")
                .node(parserContext.getFunction("to_char").node(node.child(0), new AstStringConstant(dateField))));
    }

    @Override
    protected void transformToDate(AstFunNode node)
    {
        if (node.jjtGetNumChildren() == 1)
            
node.addChild(new AstStringConstant("YYYY-MM-DD"));
    }

    @Override
    protected void transformDateFormat(AstFunNode node, DateFormat df)
    {
        String formatMask = df.getFormatOther();
        node.replaceWith(parserContext.getFunction("to_char").node(node.child(0), new AstStringConstant(formatMask, true)));
    }

    @Override
    protected void transformCast(AstCast cast)
    {
        if (cast.getDataType().equalsIgnoreCase("CHAR") || cast.getDataType().equalsIgnoreCase("VARCHAR"))
            cast.replaceWith(parserContext.getFunction("to_char").node(cast.child(0)));
        else if (cast.getDataType().equalsIgnoreCase("BIGINT"))
            cast.replaceWith(parserContext.getFunction("to_number").node(cast.child(0)));
        else if (cast.getDataType().equalsIgnoreCase("DATE"))
            cast.replaceWith(parserContext.getFunction("to_date").node(cast.child(0), new AstStringConstant("YYYY-MM-DD")));
        else if (cast.getDataType().equalsIgnoreCase("KEY"))
            cast.setDataType("VARCHAR2(15 CHAR)");
        else if (cast.getDataType().equalsIgnoreCase("DECIMAL") && cast.getSize() == 18 && cast.getScale() == 2)
            cast.replaceWith(TO_NUMBER.node(cast.child(0), new AstStringConstant("9999999999999999999.99")));
    }

    @Override
    protected void transformCastOracle(AstFunNode node)
    {
        if (node.getFunction().getName().equalsIgnoreCase("to_key"))
            node.replaceWith(new AstCast(node.child(0), "VARCHAR2(15 CHAR)"));
    }

    @Override
    protected void transformCoalesce(AstFunNode node)
    {
        if (node.jjtGetNumChildren() < 2)
            throw new IllegalStateException("COALESCE node must contain at least two arguments: " + node.format());
        int num = node.jjtGetNumChildren() - 1;
        SimpleNode lastNode = node.removeChild(num);
        while (--num >= 1)
        {
            lastNode = NVL.node(node.removeChild(num), lastNode);
        }
        node.addChild(lastNode);
        node.setFunction(NVL);
    }

    @Override
    protected void transformSelect(AstSelect select)
    {
        super.transformSelect(select);
        AstLimit limit = select.getLimit();
        if (limit != null)
        {
            select.dropLimit();
            SimpleNode parent = select.jjtGetParent();
            int idx = parent.indexOf(select);
            AstTableRef tableRef = new AstTableRef(select);
            AstFrom from = new AstFrom(tableRef);
            AstWhere where = new AstWhere();
            AstFunNode less = parserContext.getFunction("<=").node(new AstIdentifierConstant("ROWNUM"),
                    AstNumericConstant.of(limit.getLimit() + limit.getOffset()));
            where.addChild(less);
            if (limit.getOffset() == 0)
            {
                AstSelect newSelect = new AstSelect(new AstSelectList(), from, where);
                parent.jjtAddChild(newSelect, idx);
            }
            else
            {
                AstSelectList list = new AstSelectList();
                list.addChild(new AstFieldReference(new AstIdentifierConstant("tmp"), new AstIdentifierConstant("*,")));
                list.addChild(new AstIdentifierConstant("ROWNUM rn"));

                tableRef.addChild(new AstIdentifierConstant("tmp"));
                AstSelect innerSelect = new AstSelect(list, from, where);

                AstWhere outerWhere = new AstWhere();
                AstFunNode more = parserContext.getFunction(">").node(new AstIdentifierConstant("ROWNUM"),
                        AstNumericConstant.of(limit.getOffset()));
                outerWhere.addChild(more);
                AstSelect outerSelect = new AstSelect(select.getSelectList(), new AstFrom(new AstTableRef(innerSelect)), outerWhere);
                parent.jjtAddChild(outerSelect, idx);
            }
        }
    }

    @Override
    protected void transformIdentifier(AstIdentifierConstant identifier)
    {
        super.transformIdentifier(identifier);
        if (identifier.getValue().equalsIgnoreCase("current_timestamp") || identifier.getValue().equalsIgnoreCase("current_date"))
            identifier.setValue("SYSDATE");
    }

    @Override
    protected void transformString(AstStringConstant string)
    {
        for (AstStringPart child : string.children().select(AstStringPart.class))
        {
            String content = child.getContent().replace("\\'", "''");
            child.setContent(string.isEscape() ? content.replaceAll("\\\\([^bfnrt])", "$1") : content, true);
        }
        if (string.isEscape())
        {
            string.setEscape(false);
            AstConcatExpression concat = new AstConcatExpression(SqlParserTreeConstants.JJTCONCATEXPRESSION);
            if (replacedEscapes(string, concat))
                string.replaceWith(concat);
        }
    }

    private boolean replacedEscapes(AstStringConstant string, AstConcatExpression concat)
    {
        boolean find = false;
        for (int i = 0; i < string.jjtGetNumChildren(); i++)
        {
            if (string.child(i) instanceof AstStringPart)
            {
                AstStringPart child = (AstStringPart) string.child(i);
                Matcher matcher = Pattern.compile("\\\\([bfnrt])").matcher(child.getContent());
                while (matcher.find())
                {
                    find = true;
                    StringBuffer buffer = new StringBuffer();
                    matcher.appendReplacement(buffer, "");
                    for (int j = 0; j < i; j++)
                    {
                        getLastString(concat).addChild(string.child(j));
                    }
                    if (!"".equals(buffer.toString()))
                        getLastString(concat).addChild(new AstStringPart(buffer.toString(), true));
                    concat.addChild(parserContext.getFunction("CHR")
                            .node(AstNumericConstant.of(AstStringConstant.getASCII(matcher.group(1).charAt(0)))));
                }
                if (find)
                {
                    String tail = matcher.appendTail(new StringBuffer()).toString();
                    if (!"".equals(tail))
                        getLastString(concat).addChild(new AstStringPart(tail));
                    string.removeChild(child);
                }
            }
        }
        return find;
    }

    protected AstStringConstant getLastString(AstConcatExpression concat)
    {
        if (concat.jjtGetNumChildren() == 0 || !(concat.jjtGetChild(concat.jjtGetNumChildren() - 1) instanceof AstStringConstant))
            concat.addChild(new AstStringConstant(SqlParserTreeConstants.JJTSTRINGCONSTANT));

        return (AstStringConstant) concat.jjtGetChild(concat.jjtGetNumChildren() - 1);
    }

    @Override
    protected AstFrom transformDualFrom(AstFrom from)
    {
        return from == null ? AstFrom.createDual() : from;
    }

    @Override
    protected void transformNow(AstFunNode node)
    {
        node.replaceWith(new AstIdentifierConstant("SYSDATE"));
    }

    @Override
    Dbms getDbms()
    {
        return Dbms.ORACLE;
    }
}
