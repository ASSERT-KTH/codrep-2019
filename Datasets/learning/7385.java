package com.developmentontheedge.sql.format.dbms;

import com.developmentontheedge.sql.model.AstQuery;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.ParserContext;

/**
 * Interface to process DB specific issues.
 * For this purpose DB specific transformers modifies AST.
 */
public interface DbmsTransformer
{
    void transformAst(AstStart start);

    void transformQuery(AstQuery start);

    
ParserContext getParserContext();

    void setParserContext(ParserContext parserContext);
}
