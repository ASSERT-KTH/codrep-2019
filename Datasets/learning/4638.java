package com.developmentontheedge.sql.model;

import com.developmentontheedge.sql.format.dbms.Dbms;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

public class DbSpecificFunction extends PredefinedFunction
{
    private Set<Dbms> dbms;

    public DbSpecificFunction(PredefinedFunction fn, Dbms... dbms)
    {
        super(fn.getName(), fn.getPriority(), fn.minNumberOfParams(), fn.maxNumberOfParams());
        this.dbms = EnumSet.copyOf(Arrays.asList(dbms));
    }

    public DbSpecificFunction(String name, int numberOfParams)
    {
        super(name, Function.FUNCTION_PRIORITY, numberOfParams);
        this.dbms = Collections.emptySet();
    }

    public boolean isApplicable(Dbms dbms)
    {
        return this.dbms.contains(dbms);
    }

    public void setDbms(Set<Dbms> dbms)
    {
        this.dbms = dbms;
    }

    private AstBeDbmsTransform dbmsTransform;

    public void setDbmsTransform(AstBeDbmsTransform dt)
    {
        this.dbmsTransform = dt;
    }

    public SimpleNode getReplacement(AstFunNode node, Dbms dbms)
    {
        if (!isApplicable(dbms))
            throw new IllegalStateException("Function/operator '" + this.getName() + "' is unsupported for " + dbms);
        SimpleNode replacement = dbmsTransform.getValue(dbms).child(0).clone();
        AstBeDbmsArgumentList argList = (AstBeDbmsArgumentList) dbmsTransform.child(0);
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            replace(replacement, (AstIdentifierConstant) argList.child(i), node.child(i));
        }
        return replacement;
    }

    public void replace(SimpleNode node, AstIdentifierConstant target, SimpleNode replacement)
    {
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            SimpleNode child = node.child(i);
            replace(child, target, replacement);
            if (child instanceof AstIdentifierConstant && ( (AstIdentifierConstant) child).getValue().equals(target.getValue()))
                child.replaceWith(replacement.clone());
        }
    }

    public static Predicate<Function> needsTransformation(Dbms dbms)
    {
        return fn -> fn instanceof DbSpecificFunction && ((DbSpecificFunction) fn).dbmsTransform != null
                && !((AstBeDbmsThen) ((DbSpecificFunction) fn).dbmsTransform.getValue(dbms)).isAsIs();
    }
}
