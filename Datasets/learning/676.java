/* Generated By:JJTree: Do not edit this line. AstNumericConstant.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.developmentontheedge.sql.model;

import java.util.Objects;

public class AstNumericConstant extends SimpleNode
{
    public AstNumericConstant(int id)
    {
        super(id);
    }

    public AstNumericConstant(Number value)
    {
        this(SqlParserTreeConstants.JJTNUMERICCONSTANT);
        setValue(value);
    }

    private Number value;

    	public Number getValue()
    {
        return value;
    }

    public void setValue(Number val)
    {
        value = val;
    }

    @Override
    public String getNodeContent()
    {
        return value == null ? "0" : value.toString();
    }

    @Override
    public String toString()
    {
        return "Constant: " + value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final AstNumericConstant other = (AstNumericConstant) obj;
        return Objects.equals(value, other.value);
    }

    public static AstNumericConstant of(Number number)
    {
        return new AstNumericConstant(number);
    }
}
/* JavaCC - OriginalChecksum=322e3c529344c3c1ff002a508b8373af (do not edit this line) */