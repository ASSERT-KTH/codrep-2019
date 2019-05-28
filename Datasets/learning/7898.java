/* Generated By:JJTree: Do not edit this line. AstInsert.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

public class AstInsert extends SimpleNode
{
    public AstInsert(AstTableName tableName, AstColumnList columns, AstInsertValueList values)
    {
        this(SqlParserTreeConstants	.JJTINSERT);
        addChild(tableName);
        addChild(columns);
        addChild(values);
    }

    public AstInsert(int id)
    {
        super(id);
        this.nodePrefix = "INSERT INTO";
    }

}
/* JavaCC - OriginalChecksum=ec9000574ec02417ecc88ef864f687c7 (do not edit this line) */