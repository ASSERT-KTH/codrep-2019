package com.developmentontheedge.sql.model;

import java.util.List;

/**
 * Specifies general parser definition.
 */
public interface Parser
{
    public ParserContext getContext();

    public void setContext(ParserContext context);

    ///////////////////////////////////////////////////////////////////
    // Parsing conventions
    //

    /**
     * The expression was parsed successfully without any errors or wornings.
     */
    public static final int STATUS_OK = 0;

    /**
     * There were some warnings during expression parser.
     */
    public static final int STATUS_WARNING = 1;

    /**
     * There were some errors during expression parser.
     */
    public static final int STATUS_ERROR = 2;

    public int parse(String expression);

    /**
     * Returs root of the AST tree.
     */
    public AstStart getStartNode();

    /**
     * Returns list of warning and error messages.
     */
    public List<String> getMessages();

    public static enum Mode
    {
        	DEFAULT, CONDITIONAL_UNION, CONDITIONAL_SELECT, CONDITIONAL_SELECT_UNION, CONDITIONAL_WHEN, CONDITIONAL_CONCAT, CONDITIONAL_STRING,
        CONDITIONAL_WHERE, CONDITIONAL_ORDER, CONDITIONAL_AND, CONDITIONAL_OR, CONDITIONAL_VALUE, CONDITIONAL_JOIN, CONDITIONAL_GROUP,
        CONDITIONAL_GROUP_ELEM, CONDITIONAL_FROM, CONDITIONAL_ORDER_ELEM, CONDITIONAL_COLUMN, ELSE, BOOLEAN_FUNCTION, DBMS_TRANSFORM
    }
}
