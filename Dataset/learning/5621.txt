package com.developmentontheedge.sql.model;

import com.developmentontheedge.sql.format.dbms.DateFormat;
import com.developmentontheedge.sql.format.dbms.Dbms;
import one.util.streamex.StreamEx;

import java.util.HashMap;
import java.util.Locale;

/**
 *
 */
public class DefaultParserContext implements ParserContext
{
    private DefaultParserContext()
    {
        declareStandardOperators(this);
        declareSqlFunctions(this);
    }

    public static DefaultParserContext getInstance()
    {
        return instance;
    }

    ParserContext parent = null;

    public ParserContext getParentContext()
    {
        return parent;
    }

    public void setParentContext(ParserContext parent)
    {
        this.parent = parent;
    }

    ///////////////////////////////////////////////////////////////////
    // Function issues
    //

    protected HashMap<String, Function> functionsMap = new HashMap<>();

    @Override
    public Function getFunction(String name)
    {
        name = name.toLowerCase(Locale.ENGLISH);

        Function function = functionsMap.get(name);
        if (function == null && parent != null)
            function = parent.getFunction(name);

        return function;
    }

    @Override
    public void declareFunction(Function function, String... otherNames)
    {
        functionsMap.put(function.getName().toLowerCase(Locale.ENGLISH), function);
        for (String n : otherNames)
            functionsMap.put(n.toLowerCase(Locale.ENGLISH), function);
    }

    /*
     * Declares standard operators:
     *
     * @todo implement properly
     * @todo declare operator names as constants
     */
    public static final String OR = "||";
    public static final String OR_LIT = "OR";
    public static final String AND = "&&";
    public static final String AND_LIT = "AND";
    public static final String NOT = "!";
    public static final String NOT_LIT = "NOT";
    public static final String XOR = "XOR";

    public static final String GT = ">";
    public static final String LT = "<";
    public static final String GEQ = ">=";
    public static final String LEQ = "<=";
    public static final String EQ = "=";
    public static final String EQQ = "==";
    public static final String NEQ = "!=";
    public static final String LTGT = "<>";
    public static final String LIKE = "LIKE";
    public static final String NOT_LIKE = "NOT LIKE";
    public static final String IN = "IN";
    public static final String NOT_IN = "NOT IN";
    public static final String UPPER = "UPPER";
    public static final String LOWER = "LOWER";

    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String UMINUS = "u-";
    public static final String TIMES = "*";
    public static final String DIVIDE = "/";
    public static final String MOD = "%";

    public static final String BIT_AND = "&";
    public static final String BIT_OR = "|";

    public static final String REGEXP_MATCH = "~";

    public static final String OP_CONCAT = "||";

    public static final String GET_FIELD = "->";
    public static final String GET_FIELD_TXT = "->>";
    public static final String EXTRACT_PATH = "#>";
    public static final String EXTRACT_PATH_TXT = "#>>";

    public static final PredefinedFunction FUNC_OR = new PredefinedFunction(OR_LIT, Function.LOGICAL_PRIORITY, -1);
    public static final PredefinedFunction FUNC_AND = new PredefinedFunction(AND_LIT, Function.LOGICAL_PRIORITY, -1);
    public static final PredefinedFunction FUNC_NOT = new PredefinedFunction(NOT_LIT, Function.UNARY_PRIORITY, 1);
    public static final PredefinedFunction FUNC_XOR = new PredefinedFunction(XOR, Function.LOGICAL_PRIORITY, -1);

    public static final PredefinedFunction FUNC_EQ = new PredefinedFunction(EQ, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_GT = new PredefinedFunction(GT, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_LT = new PredefinedFunction(LT, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_GEQ = new PredefinedFunction(GEQ, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_LEQ = new PredefinedFunction(LEQ, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_LTGT = new PredefinedFunction(LTGT, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_LIKE = new PredefinedFunction(LIKE, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_NOT_LIKE = new PredefinedFunction(NOT_LIKE, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_IN = new PredefinedFunction(IN, Function.RELATIONAL_PRIORITY, 2);
    public static final PredefinedFunction FUNC_NOT_IN = new PredefinedFunction(NOT_IN, Function.RELATIONAL_PRIORITY, 2);

    public static final PredefinedFunction FUNC_PLUS = new PredefinedFunction(PLUS, Function.PLUS_PRIORITY, -1);
    public static final PredefinedFunction FUNC_MINUS = new PredefinedFunction(MINUS, Function.PLUS_PRIORITY, 2);
    public static final PredefinedFunction FUNC_UMINUS = new PredefinedFunction(UMINUS, Function.UNARY_PRIORITY, 1);
    public static final PredefinedFunction FUNC_TIMES = new PredefinedFunction(TIMES, Function.TIMES_PRIORITY, -1);
    public static final PredefinedFunction FUNC_DIVIDE = new PredefinedFunction(DIVIDE, Function.TIMES_PRIORITY, 2);
    public static final PredefinedFunction FUNC_MOD = new PredefinedFunction(MOD, Function.TIMES_PRIORITY, 2);

    public static final PredefinedFunction FUNC_CONCAT = new PredefinedFunction(OP_CONCAT, Function.LOGICAL_PRIORITY, 2);

    public static final PredefinedFunction FUNC_UPPER = new PredefinedFunction(UPPER, Function.FUNCTION_PRIORITY, 1);
    public static final PredefinedFunction FUNC_LOWER = new PredefinedFunction(LOWER, Function.FUNCTION_PRIORITY, 1);

    private static final DefaultParserContext instance = new DefaultParserContext();

    public static void declareStandardOperators(ParserContext context)
    {
        // Logical operators
        context.declareFunction(FUNC_OR);
        context.declareFunction(FUNC_AND, AND);
        context.declareFunction(FUNC_NOT, NOT);
        context.declareFunction(FUNC_XOR);

        // Relational operators
        context.declareFunction(FUNC_GT);
        context.declareFunction(FUNC_LT);
        context.declareFunction(FUNC_EQ, EQQ);
        context.declareFunction(FUNC_GEQ);
        context.declareFunction(FUNC_LEQ);
        context.declareFunction(FUNC_LTGT, NEQ);
        context.declareFunction(FUNC_LIKE);
        context.declareFunction(FUNC_NOT_LIKE);
        context.declareFunction(FUNC_IN);
        context.declareFunction(FUNC_NOT_IN);
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction(REGEXP_MATCH, Function.RELATIONAL_PRIORITY, 2), Dbms.POSTGRESQL, Dbms.MYSQL, Dbms.ORACLE));

        // Arithmetic operators
        context.declareFunction(FUNC_PLUS);
        context.declareFunction(FUNC_MINUS);
        context.declareFunction(FUNC_DIVIDE);
        context.declareFunction(FUNC_TIMES);
        context.declareFunction(FUNC_UMINUS);
        context.declareFunction(FUNC_MOD);
        context.declareFunction(new PredefinedFunction(BIT_AND, Function.PLUS_PRIORITY, 2));
        context.declareFunction(new PredefinedFunction(BIT_OR, Function.PLUS_PRIORITY, 2));

        // JSON
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction(GET_FIELD, Function.LOGICAL_PRIORITY, 2), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction(GET_FIELD_TXT, Function.LOGICAL_PRIORITY, 2), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction(EXTRACT_PATH, Function.LOGICAL_PRIORITY, 2), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction(EXTRACT_PATH_TXT, Function.LOGICAL_PRIORITY, 2), Dbms.POSTGRESQL));
    }

    private static void declareFunction(ParserContext context, String name, String... otherNames)
    {
        context.declareFunction(new PredefinedFunction(name, Function.FUNCTION_PRIORITY, -1), otherNames);
    }

    private static void declareFunction(ParserContext context, int numParams, String name, String... otherNames)
    {
        context.declareFunction(new PredefinedFunction(name, Function.FUNCTION_PRIORITY, numParams), otherNames);
    }

    private static void declareFunction(ParserContext context, int minParams, int maxParams, String name, String... otherNames)
    {
        context.declareFunction(new PredefinedFunction(name, Function.FUNCTION_PRIORITY, minParams, maxParams), otherNames);
    }

    public static void declareSqlFunctions(ParserContext context)
    {
        context.declareFunction(FUNC_CONCAT);

        declareFunction(context, "CONCAT");
        declareFunction(context, "LEAST");
        declareFunction(context, "GREATEST");
        declareFunction(context, "COALESCE", "IFNULL");
        declareFunction(context, "LENGTH", "LEN"); // TODO: check number of arguments

        context.declareFunction(new PredefinedFunction("SUM", Function.AGGREGATE_FUNCTION_PRIORITY, 1));
        declareFunction(context, 1, "MAX");
        declareFunction(context, 1, "MIN");

        declareFunction(context, 2, 3, "SUBSTR", "SUBSTRING");
        declareFunction(context, 2, "RIGHT");
        declareFunction(context, 2, "LEFT");

        declareFunction(context, 1, UPPER);
        declareFunction(context, 1, LOWER);
        declareFunction(context, 1, "CHR", "CHAR");

        declareFunction(context, 1, 2, "TO_CHAR");

        declareFunction(context, 1, "TO_NUMBER");
        declareFunction(context, 1, "TO_KEY");
        declareFunction(context, 3, "REPLACE");

        declareFunction(context, 1, 2, "ROUND");
        declareFunction(context, 1, 2, "TRUNC", "TRUNCATE");

        declareFunction(context, 3, "LPAD");
        declareFunction(context, 1, "LTRIM");
        declareFunction(context, 1, "RTRIM");
        declareFunction(context, 1, "TRIM");

        declareFunction(context, 2, 3, "IF");
        declareFunction(context, 2, "NULLIF");

        declareFunction(context, 0, "NOW");
        declareFunction(context, 2, "DATE_FORMAT");
        declareFunction(context, 2, "DATE_TRUNC");

        StreamEx.of(DateFormat.values()).forEach(df -> declareFunction(context, df.name()));

        declareFunction(context, 1, 2, "TO_DATE");

        declareFunction(context, 2, "INSTR", "STRPOS");
        declareFunction(context, 2, "ADD_MONTHS");
        declareFunction(context, 2, "ADD_DAYS");
        declareFunction(context, 2, "ADD_MILLIS");
        declareFunction(context, 1, "LAST_DAY");
        declareFunction(context, 1, "GROUPING");

        declareFunction(context, 2, "YEARDIFF");
        declareFunction(context, 2, "SECONDDIFF");
        declareFunction(context, 2, "MINUTEDIFF");
        declareFunction(context, 2, "HOURDIFF");
        declareFunction(context, 2, "DAYDIFF");
        declareFunction(context, 2, "MONTHDIFF");
        declareFunction(context, 2, "AGE");
        declareFunction(context, 3, "TIMESTAMPDIFF");
        declareFunction(context, 1, "FLOOR");
        declareFunction(context, 2, "MOD");
        declareFunction(context, 1, "ABS");

        declareFunction(context,0, "ROW_NUMBER");
        declareFunction(context, 0, "RANK");
        declareFunction(context, 1, "AVG");
        declareFunction(context, 3, -1, "DECODE");

        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("PG_RELATION_SIZE", Function.FUNCTION_PRIORITY, 1), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("PG_SIZE_PRETTY", Function.FUNCTION_PRIORITY, 1), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("REGEXP_INSTR", Function.FUNCTION_PRIORITY, 2), Dbms.POSTGRESQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("REGEXP_REPLACE", Function.FUNCTION_PRIORITY, 2, 3), Dbms.DB2, Dbms.POSTGRESQL, Dbms.ORACLE));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("REGEXP_LIKE", Function.FUNCTION_PRIORITY, 2), Dbms.POSTGRESQL, Dbms.MYSQL, Dbms.ORACLE));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("DENSE_RANK", Function.FUNCTION_PRIORITY, 0), Dbms.DB2, Dbms.POSTGRESQL, Dbms.ORACLE, Dbms.SQLSERVER));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("STRING_AGG", Function.AGGREGATE_FUNCTION_PRIORITY, 1, 2), Dbms.DB2, Dbms.POSTGRESQL, Dbms.ORACLE, Dbms.MYSQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("TRANSLATE", Function.FUNCTION_PRIORITY, 3), Dbms.DB2, Dbms.POSTGRESQL, Dbms.ORACLE));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("LEVENSHTEIN", Function.FUNCTION_PRIORITY, 2), Dbms.POSTGRESQL, Dbms.ORACLE));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("REVERSE", Function.FUNCTION_PRIORITY, 1), Dbms.MYSQL));
        context.declareFunction(new DbSpecificFunction(new PredefinedFunction("SUBSTRING_INDEX", Function.FUNCTION_PRIORITY, 3), Dbms.MYSQL));
    }
}
