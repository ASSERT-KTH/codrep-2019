package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstBetweenPredicate;
import com.developmentontheedge.sql.model.AstCase;
import com.developmentontheedge.sql.model.AstCaseElse;
import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstColumnList;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstExtract;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNullPredicate;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstPosition;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstSpecialConstant;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstStringPart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.AstValues;
import com.developmentontheedge.sql.model.AstWhen;
import com.developmentontheedge.sql.model.AstWhere;
import com.developmentontheedge.sql.model.AstWindowFunction;
import com.developmentontheedge.sql.model.AstWindowSpecification;
import com.developmentontheedge.sql.model.AstWith;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.Function;
import com.developmentontheedge.sql.model.PredefinedFunction;
import com.developmentontheedge.sql.model.SimpleNode;
import com.developmentontheedge.sql.model.SqlParserTreeConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlServerTransformer extends GenericDbmsTransformer
{
    private static final PredefinedFunction SUBSTRING = new PredefinedFunction("SUBSTRING", PredefinedFunction.FUNCTION_PRIORITY, 3);
    private static final PredefinedFunction LEN = new PredefinedFunction("LEN", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction CHAR = new PredefinedFunction("CHAR", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction RIGHT = new PredefinedFunction("RIGHT", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction RTRIM = new PredefinedFunction("RTRIM", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction REPLICATE = new PredefinedFunction("REPLICATE", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction GETDATE = new PredefinedFunction("GETDATE", PredefinedFunction.FUNCTION_PRIORITY, 0);
    private static final PredefinedFunction CONVERT = new PredefinedFunction("CONVERT", PredefinedFunction.FUNCTION_PRIORITY, 3);
    private static final PredefinedFunction VARCHAR = new PredefinedFunction("VARCHAR", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction DATEPART = new PredefinedFunction("DATEPART", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction DATEADD = new PredefinedFunction("DATEADD", PredefinedFunction.FUNCTION_PRIORITY, 3);
    private static final PredefinedFunction DATEDIFF = new PredefinedFunction("DATEDIFF", PredefinedFunction.FUNCTION_PRIORITY, 3);
    private static final PredefinedFunction CHARINDEX = new PredefinedFunction("CHARINDEX", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction DATENAME = new PredefinedFunction("DATENAME", PredefinedFunction.FUNCTION_PRIORITY, 2);

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
            else            {
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
    protected void transformWith(AstWith with)
    {
        with.setRecursion(false);
    }

    @Override
    protected void transformLeastGreatest(AstFunNode node)
    {
        String name = node.getFunction().getName();
        AstIdentifierConstant v = new AstIdentifierConstant("V");
        Function func;
        if ("LEAST".equals(name))
            func = parserContext.getFunction("min");
        else func = parserContext.getFunction("max");
        AstValues values = new AstValues(0);
        for (SimpleNode child : node.children())
            values.addChild(new AstParenthesis(child));
        AstTableRef tableRef = new AstTableRef(new AstParenthesis(values), new AstIdentifierConstant(name), new AstColumnList(v));
        node.replaceWith(new AstParenthesis(new AstSelect(new AstSelectList(func.node(v)), new AstFrom(tableRef))));
    }

    @Override
    protected void transformMod(AstFunNode node)
    {
        node.setFunction(DefaultParserContext.FUNC_MOD);
    }

    @Override
    protected SimpleNode getDateTimeDiff(SimpleNode startDate, SimpleNode endDate, String format)
    {
        int dp;
        switch (format)
        {
            case "YEAR":
                format = "yy";
                dp = 5;
                break;
            case "MONTH":
                format = "mm";
                dp = 2;
                break;
            case "DAY":
                return DATEDIFF.node(new AstIdentifierConstant("dd"), startDate, endDate);
            case "HOUR":
                format = "hh";
                dp = 9;
                break;
            case "MINUTE":
                format = "mi";
                dp = 6;
                break;
            case "SECOND":
                format = "ss";
                dp = 3;
                break;
            default:
                throw new IllegalStateException("Unsupported value for datepart in TIMESTAMPDIFF: " + format);
        }
        AstFunNode dateDiff = DATEDIFF.node(new AstIdentifierConstant(format), startDate, endDate);
        AstFunNode cond = parserContext.getFunction("<=").node(getDatePart(startDate, dp, format), getDatePart(endDate, dp, format));
        return new AstCase(new AstWhen(cond, dateDiff), new AstCaseElse(DefaultParserContext.FUNC_MINUS.node(dateDiff, AstNumericConstant.of(1))));
    }

    private AstFunNode getDatePart(SimpleNode node, int dp, String format)
    {
        int len;
        int style;
        switch (format)
        {
            case "yy":
            case "mm":
                len = 10;
                style = 120;
                break;
            case "hh":
            case "mi":
            case "ss":
                len = 23;
                style = 121;
                break;
            default:
                throw new IllegalStateException("Unsupported value for datepart: " + format);
        }
        return RIGHT.node(CONVERT.node(VARCHAR.node(AstNumericConstant.of(len)), node, AstNumericConstant.of(style)), AstNumericConstant.of(dp));
    }

    @Override
    protected void transformLastDay(AstFunNode node)
    {
        SimpleNode date = node.child(0);
        transformLastDay(node, date);
    }

    @Override
    protected void transformLastDayPostgres(SimpleNode node, SimpleNode date)
    {
        transformLastDay(node, date);
    }

    protected void transformLastDay(SimpleNode node, SimpleNode date)
    {
        Function opConcat = DefaultParserContext.FUNC_PLUS;
        SimpleNode dateDiff = DATEDIFF.node(new AstIdentifierConstant("MONTH"), AstNumericConstant.of(0), date);
        SimpleNode dateAdd = DATEADD.node(new AstIdentifierConstant("MONTH"), opConcat.node(dateDiff, AstNumericConstant.of(1)), AstNumericConstant.of(0));
        SimpleNode expr = DATEADD.node(new AstIdentifierConstant("DAY"), AstNumericConstant.of(-1), dateAdd);
        node.replaceWith(CONVERT.node(new AstIdentifierConstant("DATE"), expr, AstNumericConstant.of(104)));
    }

    @Override
    protected void transformDateAdd(AstFunNode node)
    {
        SimpleNode date = node.child(0);
        SimpleNode number = node.child(1);
        String name = node.getFunction().getName();
        String type = name.equalsIgnoreCase("add_months") ? "MONTH" : name.equalsIgnoreCase("add_days") ? "DAY" : "MILLISECOND";
        node.replaceWith(DATEADD.node(new AstIdentifierConstant(type), number, date));
    }

    @Override
    protected void transformPosition(AstPosition position)
    {
        position.replaceWith(CHARINDEX.node(position.child(0), position.child(1)));
    }

    @Override
    protected void transformInstr(AstFunNode node)
    {
        node.replaceWith(CHARINDEX.node(node.child(1), node.child(0)));
    }

    @Override
    protected void transformDateTrunc(AstFunNode node)
    {
        AstStringConstant child = (AstStringConstant) node.child(0);
        String dateformat = child.getValueUnescaped();
        AstIdentifierConstant datepart = new AstIdentifierConstant(dateformat);
        AstNumericConstant date = AstNumericConstant.of(0);
        node.replaceWith(DATEADD.node(datepart, DATEDIFF.node(datepart, date, node.child(1)), date));
    }

    @Override
    protected void transformExtract(AstExtract extract)
    {
        transformYearMonthDay(extract);
    }

    @Override
    protected void transformYearMonthDay(SimpleNode node)
    {
        String dateField = node instanceof AstFunNode ? ((AstFunNode) node).getFunction().getName() : ((AstExtract) node).getDateField();
        node.replaceWith(DATEPART.node(new AstIdentifierConstant(dateField), node.child(0)));
    }

    @Override
    protected void transformToDate(AstFunNode node)
    {
        node.replaceWith(CONVERT.node(new AstIdentifierConstant("DATE"), node.child(0), AstNumericConstant.of(120)));
    }

    @Override
    protected void transformDateFormat(AstFunNode node, DateFormat df)
    {
        Number length, codePage;
        switch (df)
        {
            case FORMAT_DATE:
                length = 10;
                codePage = 120;
                break;
            case FORMAT_DATETIME:
                length = 19;
                codePage = 120;
                break;
            case FORMAT_DATE_RUS:
                length = 10;
                codePage = 104;
                break;
            case FORMAT_DATE_RUS_SHORT:
                length = 8;
                codePage = 4;
                break;
            case FORMAT_MONTHYEAR:
                length = 4;
                codePage = 120;
                break;
            case FORMAT_FMDAYMONTH:
            case FORMAT_DAYMONTH:
                length = 5;
                codePage = 4;
                break;
            case FORMAT_HOURMINUTE:
                length = 5;
                codePage = 114;
                break;
            case FORMAT_YYYYMMDD:
                length = 8;
                codePage = 112;
                break;
            case FORMAT_YYYYMM:
                length = 6;
                codePage = 112;
                break;
            case FORMAT_DAY_OF_WEEK:
                node.replaceWith(DATEPART.node(new AstIdentifierConstant("dw"), node.child(0)));
                return;
            default:
                SimpleNode rtrim = RTRIM.node(parserContext.getFunction(df.name()).node(node.child(0)));
                SimpleNode padded = DefaultParserContext.FUNC_PLUS.node(new AstStringConstant("0"), rtrim);
                node.replaceWith(RIGHT.node(padded, AstNumericConstant.of(df == DateFormat.YEAR ? 4 : 2)));
                return;
        }
        SimpleNode replacement = CONVERT.node(VARCHAR.node(AstNumericConstant.of(length)), node.child(0), AstNumericConstant.of(codePage));
        if (DateFormat.FORMAT_MONTHYEAR.equals(df))
            replacement = DefaultParserContext.FUNC_PLUS.node(DATENAME.node(new AstIdentifierConstant("month"), node.child(0)), new AstStringConstant(" "), replacement);
        node.replaceWith(replacement);
    }

    @Override
    protected void transformLpad(AstFunNode node)
    {
        Function opConcat = DefaultParserContext.FUNC_PLUS;

        SimpleNode str = node.child(0);
        SimpleNode fill = node.child(2);
        SimpleNode size = node.child(1);
        SimpleNode rsize = size.clone();

        node.replaceWith(RIGHT.node(opConcat.node(REPLICATE.node(fill, size), str), rsize));
    }

    @Override
    protected void transformCast(AstCast cast)
    {
        if (cast.getDataType().equalsIgnoreCase("CHAR") || cast.getDataType().equalsIgnoreCase("VARCHAR"))
        {
            cast.setDataType("VARCHAR");
            if (cast.getSize() <= 0)
                cast.setSize(1000);
        }
        if (cast.getDataType().equalsIgnoreCase("KEY"))
            cast.setDataType("BIGINT");
        if (cast.getDataType().equalsIgnoreCase("INTEGER"))
            cast.setDataType("INT");
        else if (cast.getDataType().equalsIgnoreCase("DATE"))
            cast.replaceWith(CONVERT.node(new AstIdentifierConstant("DATE"), cast.child(0), AstNumericConstant.of(120)));
    }

    @Override
    protected void transformCastOracle(AstFunNode node)
    {
        String name = node.getFunction().getName();
        if (name.equalsIgnoreCase("to_char"))
            node.replaceWith(new AstCast(node.child(0), "VARCHAR", 1000));
        else if (name.equalsIgnoreCase("to_number") || name.equalsIgnoreCase("to_key"))
            node.replaceWith(new AstCast(node.child(0), "BIGINT"));
        else
            throw new IllegalArgumentException("name = " + name);
    }

    @Override
    protected void transformChr(AstFunNode node)
    {
        node.setFunction(CHAR);
    }

    @Override
    protected void transformSubstr(AstFunNode node)
    {
        node.setFunction(SUBSTRING);
        if (node.jjtGetNumChildren() == 2)
            node.addChild(AstNumericConstant.of(100000));
    }

    @Override
    protected void transformLength(AstFunNode node)
    {
        node.setFunction(LEN);
    }

    @Override
    protected void transformSelect(AstSelect select)
    {
        AstLimit limit = select.getLimit();
        if (limit != null)
        {
            if (limit.getOffset() == 0)
            {
                limit.setShape("TOP", null);
                select.moveToFront(limit);
            }
            else
            {
                if (select.getOrderBy() == null)
                    throw new IllegalStateException("The ranking function \"ROW_NUMBER\" must have an ORDER BY clause");
                select.dropLimit();
                SimpleNode parent = select.jjtGetParent();
                int idx = parent.indexOf(select);
                AstIdentifierConstant tmp = new AstIdentifierConstant("tmp");
                AstIdentifierConstant rn = new AstIdentifierConstant("rn");

                AstSelectList list = select.getSelectList();

                AstTableRef tableRef = new AstTableRef(select);
                tableRef.setAsToken(true);
                tableRef.addChild(tmp);
                AstFrom from = new AstFrom(tableRef);

                AstWhere where = new AstWhere();
                AstBetweenPredicate between = new AstBetweenPredicate(new AstFieldReference(tmp, rn),
                        AstNumericConstant.of(limit.getOffset()), AstNumericConstant.of(limit.getOffset() + limit.getLimit()));
                where.addChild(between);

                AstSelect newSelect = new AstSelect((AstSelectList) list.clone(), from, where);

                AstFunNode func = parserContext.getFunction("row_number").node();
                AstDerivedColumn derCol = new AstDerivedColumn(new AstWindowFunction(func, new AstWindowSpecification(select.getOrderBy())));
                select.dropOrder();
                derCol.setPrefixComma(true);
                derCol.setAsToken(true);
                derCol.addChild(rn);
                if (list.isAllColumns())
                {
                    for (AstTableRef tr : select.getFrom().tableRefs())
                    {
                        String tableName = tr.getAlias() == null ? tr.getTable() : tr.getAlias();
                        list.addChild(new AstFieldReference(new AstIdentifierConstant(tableName), new AstIdentifierConstant("*")));
                    }
                }
                list.addChild(derCol);
                parent.jjtAddChild(newSelect, idx);
            }
        }
        super.transformSelect(select);
    }

    @Override
    protected AstFrom transformDualFrom(AstFrom from)
    {
        return null;
    }

    @Override
    protected void transformTrunc(AstFunNode node)
    {
        node.setFunction(parserContext.getFunction("round"));
        node.addChild(AstNumericConstant.of(1));
    }

    @Override
    protected void transformConcat(AstFunNode node)
    {
        node.setFunction(DefaultParserContext.FUNC_PLUS);
    }

    @Override
    protected void transformIdentifier(AstIdentifierConstant identifier)
    {
        super.transformIdentifier(identifier);
        if (identifier.getValue().equalsIgnoreCase("current_timestamp"))
            identifier.replaceWith(GETDATE.node());
        if (identifier.getValue().equalsIgnoreCase("current_date"))
            identifier.replaceWith(new AstCast(GETDATE.node(), "DATE"));
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
            AstFunNode concat = DefaultParserContext.FUNC_PLUS.node();
            if (replacedEscapes(string, concat))
                string.replaceWith(concat);
        }
    }

    private boolean replacedEscapes(AstStringConstant string, AstFunNode concat)
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
                    concat.addChild(CHAR.node(AstNumericConstant.of(AstStringConstant.getASCII(matcher.group(1).charAt(0)))));
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

    protected AstStringConstant getLastString(AstFunNode concat)
    {
        if (concat.jjtGetNumChildren() == 0 || !(concat.jjtGetChild(concat.jjtGetNumChildren() - 1) instanceof AstStringConstant))
            concat.addChild(new AstStringConstant(SqlParserTreeConstants.JJTSTRINGCONSTANT));

        return (AstStringConstant) concat.jjtGetChild(concat.jjtGetNumChildren() - 1);
    }

    @Override
    protected void transformNow(AstFunNode node)
    {
        node.setFunction(GETDATE);
    }

    @Override
    Dbms getDbms()
    {
        return Dbms.SQLSERVER;
    }
}
