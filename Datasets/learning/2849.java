package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstBetweenPredicate;
import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstConcatExpression;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstExtract;
import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstFirstDayOf;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstLimit;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstOrderedSetAggregate;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstPosition;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstSelectList;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstStringPart;
import com.developmentontheedge.sql.model.AstTableRef;
import com.developmentontheedge.sql.model.AstWhere;
import com.developmentontheedge.sql.model.AstWindowFunction;
import com.developmentontheedge.sql.model.AstWindowSpecification;
import com.developmentontheedge.sql.model.AstWith;
import com.developmentontheedge.sql.model.AstWithinGroup;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.Function;
import com.developmentontheedge.sql.model.PredefinedFunction;
import com.developmentontheedge.sql.model.SimpleNode;
import com.developmentontheedge.sql.model.SqlParserTreeConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB2Transformer extends GenericDbmsTransformer
{
    private static final PredefinedFunction RTRIM = new PredefinedFunction("RTRIM", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction VARCHAR_FORMAT = new PredefinedFunction("VARCHAR_FORMAT", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction DAY = new PredefinedFunction("DAY", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction MONTH = new PredefinedFunction("MONTH", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction DAYS = new PredefinedFunction("DAYS", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction HOUR = new PredefinedFunction("HOUR", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction MINUTE = new PredefinedFunction("MINUTE", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction SECOND = new PredefinedFunction("SECOND", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction BITAND = new PredefinedFunction("BITAND", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction BITOR = new PredefinedFunction("BITOR", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction LISTAGG = new PredefinedFunction("LISTAGG", PredefinedFunction.AGGREGATE_FUNCTION_PRIORITY, 2);

    @Override
    protected void transformTranslate(AstFunNode node)
    {
        node.addChild(node.removeChild(1));
    }

    @Override
    protected void transformStringAgg(AstFunNode node)
    {
        node.setFunction(LISTAGG);
        if (node.isDistinct())
            throw new IllegalStateException("DISTINCT clause is unsupported for " + node.getFunction().getName());
        if (node.child(node.jjtGetNumChildren() - 1) instanceof AstOrderBy)
        {
            AstOrderBy orderBy = (AstOrderBy) node.child(node.jjtGetNumChildren() - 1);
            node.removeChild(node.jjtGetNumChildren() - 1);
            node.replaceWith(new AstOrderedSetAggregate((AstFunNode) node.clone(), new AstWithinGroup(orderBy)));
        }
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
        node.setFunction(BITOR);
    }

    @Override
    protected void transformLastDayPostgres(SimpleNode node, SimpleNode date)
    {
        node.replaceWith(parserContext.getFunction("last_day").node(date));
    }

    @Override
    protected SimpleNode getDateTimeDiff(SimpleNode startDate, SimpleNode endDate, String format)
    {
        Function opMinus = DefaultParserContext.FUNC_MINUS;
        switch (format)
        {
            case "YEAR":
                return opMinus.node(getYears(endDate), getYears(startDate));
            case "MONTH":
                return opMinus.node(new AstParenthesis(getMonths(endDate)), new AstParenthesis(getMonths(startDate)));
            case "DAY":
                return opMinus.node(DAYS.node(endDate), DAYS.node(startDate));
            case "HOUR":
                return opMinus.node(new AstParenthesis(getHours(endDate)), new AstParenthesis(getHours(startDate)));
            case "MINUTE":
                return opMinus.node(new AstParenthesis(getMinutes(endDate)), new AstParenthesis(getMinutes(startDate)));
            case "SECOND":
                return opMinus.node(new AstParenthesis(getSeconds(endDate)), new AstParenthesis(getSeconds(startDate)));
            default:
                throw new IllegalStateException("Unsupported value for datepart in TIMESTAMPDIFF: " + format);
        }
    }

    private SimpleNode getYears(SimpleNode date)
    {
        return parserContext.getFunction("year").node(date);
    }

    private SimpleNode getMonths(SimpleNode date)
    {
        return DefaultParserContext.FUNC_PLUS.node(DefaultParserContext.FUNC_TIMES.node(AstNumericConstant.of(12), getYears(date)), MONTH.node(date));
    }

    private SimpleNode getHours(SimpleNode date)
    {
        return DefaultParserContext.FUNC_PLUS.node(DefaultParserContext.FUNC_TIMES.node(AstNumericConstant.of(24), DAYS.node(date)), HOUR.node(date));
    }

    private SimpleNode getMinutes(SimpleNode date)
    {
        return DefaultParserContext.FUNC_PLUS.node(DefaultParserContext.FUNC_TIMES.node(AstNumericConstant.of(60), new AstParenthesis(getHours(date))), MINUTE.node(date));
    }

    private SimpleNode getSeconds(SimpleNode date)
    {
        return DefaultParserContext.FUNC_PLUS.node(DefaultParserContext.FUNC_TIMES.node(AstNumericConstant.of(60), new AstParenthesis(getMinutes(date))), SECOND.node(date));
    }

    @Override
    protected void transformDateAdd(AstFunNode node)
    {
        Function opPlus = DefaultParserContext.FUNC_PLUS;
        Function opTimes = DefaultParserContext.FUNC_TIMES;
        SimpleNode date = node.child(0);
        SimpleNode number = node.child(1);
        String name = node.getFunction().getName();
        String type = name.equalsIgnoreCase("add_months") ? "MONTHS" : name.equalsIgnoreCase("add_days") ? "DAYS" : "MICROSECONDS";

        if (type.equals("MICROSECONDS"))
            number = new AstParenthesis(opTimes.node(number, AstNumericConstant.of(1000)));
        node.replaceWith(new AstParenthesis(opPlus.node(date, new AstFirstDayOf(number, type))));
    }

    @Override
    protected void transformDateTrunc(AstFunNode node)
    {
        Function opConcat = DefaultParserContext.FUNC_MINUS;
        SimpleNode date = node.child(1);
        String field = ((AstStringConstant) node.child(0)).getValue();
        AstFirstDayOf month = new AstFirstDayOf(new AstParenthesis(opConcat.node(DAY.node(date), AstNumericConstant.of(1))), "DAYS");
        if (field.equalsIgnoreCase("'MONTH'"))
            node.replaceWith(opConcat.node(date, month));
        else
        {
            AstFirstDayOf year = new AstFirstDayOf(new AstParenthesis(opConcat.node(MONTH.node(date), AstNumericConstant.of(1))), "MONTHS");
            node.replaceWith(opConcat.node(date, opConcat.node(year, month)));
        }
    }

    @Override
    protected void transformPosition(AstPosition position)
    {
        position.replaceWith(parserContext.getFunction("instr").node(position.child(1), position.child(0)));
    }

    @Override
    protected void transformExtract(AstExtract extract)
    {
        String dateField = extract.getDateField();
        extract.replaceWith(parserContext.getFunction(dateField).node(extract.child(0)));
    }

    @Override
    protected void transformToDate(AstFunNode node)
    {
        node.replaceWith(node.child(0));
    }

    @Override
    protected void transformDateFormat(AstFunNode node, DateFormat df)
    {
        switch (df)
        {
            case FORMAT_DATE:
                node.replaceWith(new AstCast(node.child(0), "CHAR", 10));
                break;
            case FORMAT_DATETIME:
                node.replaceWith(new AstCast(node.child(0), "CHAR", 19));
                break;
            case FORMAT_FMDAYMONTH:
                node.replaceWith(VARCHAR_FORMAT.node(node.child(0), new AstStringConstant(DateFormat.FORMAT_DAYMONTH.getFormatOther(), true)));
                break;
            default:
                node.replaceWith(VARCHAR_FORMAT.node(node.child(0), new AstStringConstant(df.getFormatOther(), true)));
                break;
        }
    }

    @Override
    protected void transformCast(AstCast cast)
    {
        if (cast.getDataType().equalsIgnoreCase("DATE"))
            cast.replaceWith(cast.child(0));
        else if (cast.getDataType().equalsIgnoreCase("CHAR") || cast.getDataType().equalsIgnoreCase("VARCHAR"))
        {
            cast.wrapWith(new AstFunNode(RTRIM));
            cast.setDataType("CHAR");
            if (cast.getSize() == -1)
                cast.setSize(254);
        }
        else if (cast.getDataType().equalsIgnoreCase("KEY"))
            cast.setDataType("BIGINT");
    }

    @Override
    protected void transformCastOracle(AstFunNode node)
    {
        String name = node.getFunction().getName();
        if (name.equalsIgnoreCase("to_char"))
            node.replaceWith(RTRIM.node(new AstCast(node.child(0), "CHAR", 254)));
        else if (name.equalsIgnoreCase("to_number") || name.equalsIgnoreCase("to_key"))
            node.replaceWith(new AstCast(node.child(0), "BIGINT"));
        else
            throw new IllegalArgumentException("name = " + name);
    }

    @Override
    protected void transformString(AstStringConstant string)
    {
        for (AstStringPart child : string.children().select(AstStringPart.class))
        {
            String content 	= child.getContent().replace("\\'", "''");
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
    protected void transformSelect(AstSelect select)
    {
        AstLimit limit = select.getLimit();
        if (limit != null)
        {
            if (limit.getOffset() == 0)
                limit.setShape("FETCH FIRST", "ROWS ONLY");
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
        AstFrom dual = AstFrom.createDual();
        dual.tableRefs().forEach(t -> t.setTable("SYSIBM.SYSDUMMY1"));
        return dual;
    }

    @Override
    protected void transformNow(AstFunNode node)
    {
        node.replaceWith(new AstIdentifierConstant("CURRENT_TIMESTAMP"));
    }

    @Override
    Dbms getDbms()
    {
        return Dbms.DB2;
    }
}
