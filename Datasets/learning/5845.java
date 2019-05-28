package com.developmentontheedge.sql.model;

import com.developmentontheedge.sql.format.dbms.Dbms;

import java.util.EnumSet;
import java.util.Map;

public class AstBeDbmsTransform extends SimpleNode
{
    public AstBeDbmsTransform(int id)
    {
        super(id);
        this.nodeSuffix = "END";
    }

    public void setFunctionName(String function)
    {
        this.nodePrefix = "DBMS_TRANSFORM " + function;
    }

    private DbSpecificFunction function;

    public void setFunction(DbSpecificFunction function)
    {
        this.function = function;
    }

    public DbSpecificFunction getFunction()
    {
        return this.function;
    }

    private Map<Dbms, SimpleNode> replacements;

    public void init()
    {
        if (replacements != null)
            throw new IllegalStateException();
        replacements = this.tree()
                .select(AstBeDbmsWhen.class)
                .cross(SimpleNode::children)
                .mapKeys(when -> when.parent.child(when.parent.indexOf(when) + 1))
                .mapValues(child -> getDbms(child))
                .invert().toMap();
        function.setDbms(EnumSet.copyOf(this.replacements.keySet()));
        function.setDbmsTransform(this);
    }

    private static Dbms getDbms(SimpleNode child)
    {
        String s = ((AstStringConstant) child).getValueUnescaped().toLowerCase();
        switch (s)
        {
            case "mysql":
                return Dbms.MYSQL;
            case "db2":
                return Dbms.DB2;
            case "oracle":
                return Dbms.ORACLE;
            case "postgres":
                return Dbms.POSTGRESQL;
            case "sqlserver":
                return Dbms.SQLSERVER;
            default:
                throw new IllegalArgumentException("Unknown DBMS " + s
                        + ". Was expecting on of: 'db2', 'mysql', 'oracle', 'postgres', 'sqlserver'");
        }
    }

    public SimpleNode getValue(Dbms dbms )
    {
        return replacements.get(dbms);
    }
}
