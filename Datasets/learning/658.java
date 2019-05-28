package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstCase;
import com.developmentontheedge.sql.model.AstCaseElse;
import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstExtract;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstInterval;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql. model.AstNullPredicate;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstPosition;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSpecialConstant;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstWhen;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.Function;
import com.developmentontheedge.sql.model.PredefinedFunction;
import com.developmentontheedge.sql.model.SimpleNode;

public class PostgreSqlTransformer extends GenericDbmsTransformer
{
    private static final PredefinedFunction FLOOR = new PredefinedFunction("FLOOR", PredefinedFunction.FUNCTION_PRIORITY, 1);

    @Override
    protected void transformRegexpLike(AstFunNode node)
    {
        if ("regexp_like".equalsIgnoreCase(node.getFunction().getName()))
            node.setFunction(parserContext.getFunction("~"));
    }

    @Override
    protected void transformDecode(AstFunNode node)
    {
        AstCase astCase = new AstCase();
        for (int i = 1; i < node.jjtGetNumChildren() - 1; i++)
        {
            SimpleNode cond;
            if (node.child(i) instanceof AstSpecialConstant)
            {
                cond = new AstNullPredicate(0);
                cond.addChild(node.child(0));
            }
            else
            {
                cond = DefaultParserContext.FUNC_EQ.node(node.child(0), node.child(i));
            }
            astCase.addChild(new AstWhen(cond, node.child(++i)));
            if (i == node.jjtGetNumChildren() - 2)
            {
                astCase.addChild(new AstCaseElse(node.child(++i)));
            }
        }
        node.replaceWith(astCase);
    }

    @Override
    protected SimpleNode getDateTimeDiff(SimpleNode startDate, SimpleNode endDate, String format)
    {
        switch (format)
        {
            case "YEAR":
                return getYears(endDate, startDate);
            case "MONTH":
                return DefaultParserContext.FUNC_PLUS.node(
                        new AstExtract("MONTH", parserContext.getFunction("age").node(endDate, startDate)),
                        DefaultParserContext.FUNC_TIMES.node(AstNumericConstant.of(12), getYears(endDate, startDate)));
            case "DAY":
                return new AstExtract(format, new AstParenthesis(DefaultParserContext.FUNC_MINUS.node(endDate, startDate)));
            case "HOUR":
                return FLOOR.node(DefaultParserContext.FUNC_DIVIDE.node(getSeconds(endDate, startDate), AstNumericConstant.of(3600)));
            case "MINUTE":
                return FLOOR.node(DefaultParserContext.FUNC_DIVIDE.node(getSeconds(endDate, startDate), AstNumericConstant.of(60)));
            case "SECOND":
                return getSeconds(endDate, startDate);
            default:
                throw new IllegalStateException("Unsupported value for datepart in TIMESTAMPDIFF: " + format);
        }
    }

    private SimpleNode getSeconds(SimpleNode endDate, SimpleNode startDate)
    {
        return new AstExtract("EPOCH", new AstParenthesis(DefaultParserContext.FUNC_MINUS.node(endDate, startDate)));
    }

    private SimpleNode getYears(SimpleNode endDate, SimpleNode startDate)
    {
        return new AstExtract("YEAR", parserContext.getFunction("age").node(endDate, startDate));
    }

    @Override
    protected void transformLastDay(AstFunNode node)
    {
        Function opPlus = DefaultParserContext.FUNC_PLUS;
        Function opMinus = DefaultParserContext.FUNC_MINUS;
        SimpleNode dateTrunc = parserContext.getFunction("date_trunc").node(new AstStringConstant("MONTH"), node.child(0));
        AstInterval monthInterval = new AstInterval(new AstStringConstant("1 MONTH"));
        AstInterval dayInterval = new AstInterval(new AstStringConstant("1 DAY"));
        SimpleNode expr = opPlus.node(dateTrunc, opMinus.node(monthInterval, dayInterval));
        node.replaceWith(new AstCast(expr, "DATE"));
    }

    @Override
    protected void transformDateAdd(AstFunNode node)
    {
        Function opPlus = DefaultParserContext.FUNC_PLUS;
        Function opTimes = DefaultParserContext.FUNC_TIMES;
        SimpleNode date = node.child(0);
        SimpleNode number = new AstParenthesis(node.child(1));
        String name = node.getFunction().getName();
        String type = name.equalsIgnoreCase("add_months") ? "1 MONTH" : name.equalsIgnoreCase("add_days") ? "1 DAY" : "1 MILLISECOND";
        AstInterval interval = new AstInterval(new AstStringConstant(type));

        node.replaceWith(new AstParenthesis(opPlus.node(date, opTimes.node(interval, number))));
    }

    @Override
    protected void transformInstr(AstFunNode node)
    {
        node.replaceWith(new AstPosition(node.child(1), node.child(0)));
    }

    @Override
    protected void transformYearMonthDay(SimpleNode node)
    {
        String dateField = ((AstFunNode) node).getFunction().getName();
        node.replaceWith(new AstExtract(dateField, node.child(0)));
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
        if (cast.getDataType().equalsIgnoreCase("CHAR"))
            cast.setDataType("VARCHAR");
        else if (cast.getDataType().equalsIgnoreCase("DATE"))
            cast.replaceWith(parserContext.getFunction("to_date").node(cast.child(0), new AstStringConstant("YYYY-MM-DD")));
        else if (cast.getDataType().equalsIgnoreCase("KEY"))
            cast.setDataType("BIGINT");
    }

    @Override
    protected void transformCastOracle(AstFunNode node)
    {
        String dataType;
        String name = node.getFunction().getName();
        if (name.equalsIgnoreCase("to_char"))
            dataType = "VARCHAR";
        else if (name.equalsIgnoreCase("to_number") || name.equalsIgnoreCase("to_key"))
            dataType = "BIGINT";
        else
            throw new IllegalArgumentException("name = " + name);

        node.replaceWith(new AstCast(node.child(0), dataType));
    }

    @Override
    protected void transformSelect(AstSelect select)
    {
        AstLimit limit = select.getLimit();
        if (limit != null && limit.getOffset() != 0)
            limit.setShape("LIMIT", "OFFSET " + String.valueOf(limit.getOffset()));
        super.transformSelect(select);
    }

    @Override
    protected AstFrom transformDualFrom(AstFrom from)
    {
        return null;
    }

    @Override
    protected void transformNow(AstFunNode node)
    {
        node.replaceWith(new AstIdentifierConstant("CURRENT_TIMESTAMP"));
    }

    @Override
    Dbms getDbms()
    {
        return Dbms.POSTGRESQL;
    }
}
