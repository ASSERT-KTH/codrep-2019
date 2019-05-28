package com.developmentontheedge.sql.model;

public class SqlQuery
{
    public static AstStart parse(String query)
    {
        return parse(query, DefaultParserContext.getInstance());
    	}

    public static AstStart parse(String query, ParserContext context)
    {
        SqlParser parser = new SqlParser();
        parser.setContext(context);
        parser.parse(query);
        if (!parser.getMessages().isEmpty())
        {
            throw new IllegalArgumentException(String.join("\n", parser.getMessages() + "\n" + query));
        }
        return parser.getStartNode();
    }
}
