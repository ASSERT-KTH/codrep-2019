package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstCase;
import com.developmentontheedge.sql.model.AstCaseElse;
import com.developmentontheedge.sql.model.AstCast;
import com.developmentontheedge.sql.model.AstDateAdd;
import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstExcept;
import com.developmentontheedge.sql.model.AstExtract;
import com.developmentontheedge.sql.model.AstFrom;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstIdentifierConstant.QuoteSymbol;
import com.developmentontheedge.sql.model.AstInterval;
import com.developmentontheedge.sql.model.AstNullPredicate;
import com.developmentontheedge.sql.model.AstNumericConstant;
import com.developmentontheedge.sql.model.AstOrderBy;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstPosition;
import com.developmentontheedge.sql.model.AstSpecialConstant;
import com.developmentontheedge.sql.model.AstStringConstant;
import com.developmentontheedge.sql.model.AstStringPart;
import com.developmentontheedge.sql.model.AstWhen;
import com.developmentontheedge.sql.model.AstWith;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.Function;
import com.developmentontheedge.sql.model.Node;
import com.developmentontheedge.sql.model.PredefinedFunction;
import com.developmentontheedge.sql.model.SimpleNode;

import java.util.List;
import java.util.stream.Stream;

public class MySqlTransformer extends GenericDbmsTransformer
{
    private static final PredefinedFunction CHAR = new PredefinedFunction("CHAR", PredefinedFunction.FUNCTION_PRIORITY, 1);
    private static final PredefinedFunction TRUNCATE = new PredefinedFunction("TRUNCATE", PredefinedFunction.FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction SUBSTRING = new PredefinedFunction("SUBSTRING", PredefinedFunction.FUNCTION_PRIORITY, -1);
    private static final PredefinedFunction TIMESTAMPDIFF = new PredefinedFunction("TIMESTAMPDIFF", PredefinedFunction.FUNCTION_PRIORITY, 3);
    private static final PredefinedFunction GROUP_CONCAT = new PredefinedFunction("GROUP_CONCAT", PredefinedFunction.AGGREGATE_FUNCTION_PRIORITY, 2);
    private static final PredefinedFunction REGEXP = new PredefinedFunction("REGEXP", PredefinedFunction.RELATIONAL_PRIORITY, 2);

    @Override
    protected void transformExcept(AstExcept node)
    {
        throw new IllegalStateException("Operator 'EXCEPT' is unsupported for " + getDbms());
    }

    @Override
    protected void transformRegexpLike(AstFunNode node)
    {
        node.setFunction(REGEXP);
    }

    @Override
    protected void transformStringAgg(AstFunNode node)
    {
        node.setFunction(GROUP_CONCAT);
        node.setDistinct(node.isDistinct());
        if (node.jjtGetNumChildren() > 1 && !(node.child(1) instanceof AstOrderBy))
        {
            node.addChild(node.child(1).clone());
            node.removeChild(1);
        }
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
    protected void transformWith(AstWith with)
    {
        throw new UnsupportedOperationException("WITH clause is not supported for MySQL");
    }

    @Override
    protected void transformLastDayPostgres(SimpleNode node, SimpleNode date)
    {
        node.replaceWith(parserContext.getFunction("last_day").node(date));
    }

    @Override
    protected SimpleNode getDateTimeDiff(SimpleNode startDate, SimpleNode endDate, String format)    {
        return TIMESTAMPDIFF.node(new AstIdentifierConstant(format), startDate, endDate);
    }

    @Override
    protected void transformDateAdd(AstFunNode node)
    {
        Function opTimes = DefaultParserContext.FUNC_TIMES;
        SimpleNode date = node.child(0);
        SimpleNode number = node.child(1);
        String name = node.getFunction().getName();
        String type = name.equalsIgnoreCase("add_months") ? "MONTH" : name.equalsIgnoreCase("add_days") ? "DAY" : "MICROSECOND";
        if (type.equals("MICROSECOND"))
            number = new AstParenthesis(opTimes.node(number, AstNumericConstant.of(1000)));
        node.replaceWith(new AstDateAdd(date, new AstInterval(number), type));
    }

    @Override
    protected void transformPosition(AstPosition position)
    {
        position.replaceWith(parserContext.getFunction("instr").node(position.child(1), position.child(0)));
    }

    @Override
    protected void transformDateTrunc(AstFunNode node)
    {
        AstStringConstant child = (AstStringConstant) node.child(0);
        String dateformat = child.getValue().equalsIgnoreCase("'MONTH'") ? "%Y-%m-01" : "%Y-01-01";
        node.replaceWith(parserContext.getFunction("date_format").node(node.child(1), new AstStringConstant(dateformat)));
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
        transformCastOracle(node);
    }

    @Override
    protected void transformDateFormat(AstFunNode node, DateFormat df)
    {
        String formatMask = df.getFormatMySQL();
        node.replaceWith(parserContext.getFunction("date_format").node(node.child(0), new AstStringConstant(formatMask, true)));
    }

    @Override
    protected void transformCast(AstCast cast)
    {
        if (cast.getDataType().equalsIgnoreCase("VARCHAR"))
            cast.setDataType("CHAR");
        if (cast.getDataType().equalsIgnoreCase("BIGINT") || cast.getDataType().equalsIgnoreCase("INTEGER"))
            cast.setDataType("SIGNED");
        if (cast.getDataType().equalsIgnoreCase("KEY"))
            cast.setDataType("UNSIGNED");
    }

    @Override
    protected void transformCastOracle(AstFunNode node)
    {
        String dataType;
        String name = node.getFunction().getName();
        if (name.equalsIgnoreCase("to_char"))
            dataType = "CHAR";
        else if (name.equalsIgnoreCase("to_number"))
            dataType = "SIGNED";
        else if (name.equalsIgnoreCase("to_date"))
            dataType = "DATE";
        else if (name.equalsIgnoreCase("to_key"))
            dataType = "UNSIGNED";
        else
            throw new IllegalArgumentException("name = " + name);

        node.replaceWith(new AstCast(node.child(0), dataType));
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
    }

    @Override
    protected void transformConcat(AstFunNode node)
    {
        if (isConcat(node))
            return;
        node.setFunction(parserContext.getFunction("concat"));
        List<SimpleNode> flatChildren = node.children().flatMap(child -> isConcat(child) ? child.children() : Stream.of(child))
                .toList();
        node.removeChildren();
        flatChildren.forEach(node::addChild);
        SimpleNode parent = node.jjtGetParent();
        if (parent instanceof AstParenthesis)
            parent.replaceWith(node);
    }

    protected boolean isConcat(SimpleNode node)
    {
        return node instanceof AstFunNode && ((AstFunNode) node).getFunction().getName().equalsIgnoreCase("concat");
    }

    @Override
    protected void transformIdentifier(AstIdentifierConstant identifier)
    {
        if (identifier.getQuoteSymbol() == QuoteSymbol.DOUBLE_QUOTE)
        {
            Node parent = identifier.jjtGetParent();
            if (parent instanceof AstDerivedColumn)
                return;
            identifier.setQuoteSymbol(QuoteSymbol.BACKTICK);
        }
        if (identifier.getValue().equalsIgnoreCase("current_timestamp"))
        {
            identifier.replaceWith(parserContext.getFunction("now").node());
        }
    }

    @Override
    protected void transformString(AstStringConstant string)
    {
        for (AstStringPart child : string.children().select(AstStringPart.class))
        {
            if (string.isEscape())
            {
                string.setEscape(false);
            }
            else
            {
                child.setContent(child.getContent().replace("\\", "\\\\"), true);
            }
            child.setContent(child.getContent().replaceAll("\\\\([_%])", "$1"), true);
        }
    }

    @Override
    protected void transformTrunc(AstFunNode node)
    {
        node.setFunction(TRUNCATE);
    }

    @Override
    protected void transformIf(AstFunNode node)
    {
    }

    @Override
    protected AstFrom transformDualFrom(AstFrom from)
    {
        return from == null ? AstFrom.createDual() : from;
    }

    @Override
    Dbms getDbms()
    {
        return Dbms.MYSQL;
    }
}
