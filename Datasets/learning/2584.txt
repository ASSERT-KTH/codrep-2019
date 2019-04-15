package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstBooleanExpression;
import com.developmentontheedge.sql.model.AstBooleanNot;
import com.developmentontheedge.sql.model.AstBooleanTerm;
import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.AstIdentifierConstant;
import com.developmentontheedge.sql.model.AstInPredicate;
import com.developmentontheedge.sql.model.AstParenthesis;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstWhere;
import com.developmentontheedge.sql.model.SimpleNode;

public class Simplifier
{
    public static void simplify(AstStart start)
    {
        start.tree().select(AstWhere.class).toList().forEach(Simplifier::simplifyWhere);
    }

    private static void simplifyWhere(AstWhere where)
    {
        if (where.jjtGetNumChildren() == 1)
        {
            simplifyBoolean(where.child(0));
            if (where.child(0) instanceof AstParenthesis)
            {
                where.child(0).replaceWith(where.child(0).child(0));
            }
            if (where.child(0).format().trim().equals("TRUE"))
                where.remove();
        }
    }

    private static void simplifyBoolean(SimpleNode node)
    {
        if (node instanceof AstBooleanTerm)
        {
            simplifyAnd(node);
        }
        else if (node instanceof AstBooleanExpression)
        {
            simplifyOr(node);
        }
        else if (node instanceof AstBooleanNot)
        {
            simplifyNot((AstBooleanNot) node);
        }
        else if (node instanceof AstFunNode)
        {
            simplifyFunc((AstFunNode) node);
        }
        else if (node instanceof AstParenthesis)
        {
            simplifyBoolean(node.child(0));
            if (node.child(0) instanceof AstIdentifierConstant)
            {
                node.replaceWith(node.child(0));
            }
        }
        else if (node instanceof AstInPredicate)
        {
            AstInPredicate in = (AstInPredicate) node;
            SimpleNode val = node.child(0);
            String valStr = val.format().trim();
            SimpleNode vals = node.child(1);
            if (vals.children().map(v -> v.format().trim()).anyMatch(valStr::equals))
            {
                in.replaceWith(new AstIdentifierConstant(in.isInversed() ? "FALSE" : "TRUE"));
            }
        }
    }

    private static void simplifyFunc(AstFunNode fun)
    {
        if (fun.getFunction().getName().equals("="))
        {
            if (fun.child(0).format().trim().equals(fun.child(1).format().trim()))
            {
                fun.replaceWith(new AstIdentifierConstant("TRUE"));
            }
        }
        else if (fun.getFunction().getName().equals("<>"))
        {
            if (fun.child(0).format().trim().equals(fun.child(1).format().trim()))
            {
                fun.replaceWith(new AstIdentifierConstant("FALSE"));
            }
        }
        else if (fun.getFunction().getName().equals("AND"))
        {
            simplifyAnd(fun);
        }
        else if (fun.getFunction().getName().equals("OR"))
        {
            simplifyOr(fun);
        }
    }

    private static void simplifyNot(AstBooleanNot node)
    {
        node.children().toList().forEach(Simplifier::simplifyBoolean);
        if (node.jjtGetNumChildren() == 1)
        {
            SimpleNode child = node.child(0);
            String childStr = child.format().trim();
            if (childStr.equals("TRUE"))
                child.jjtGetParent().replaceWith(new AstIdentifierConstant("FALSE"));
            else if (childStr.equals("FALSE"))
                child.jjtGetParent().replaceWith(new AstIdentifierConstant("TRUE"));
        }
    }

    private static void simplifyOr(SimpleNode node)
    {
        node.children().toList().forEach(Simplifier::simplifyBoolean)
;
        for (SimpleNode child : node.children().toList())
        {
            String childStr = child.format().trim();
            if (childStr.equals("TRUE"))
            {
                node.replaceWith(child);
                return;
            }
            if (childStr.equals("FALSE") && node.jjtGetNumChildren() > 1)
            {
                child.remove();
            }
        }
        if (node.jjtGetNumChildren() == 1)
        {
            node.replaceWith(node.child(0));
        }
    }

    private static void simplifyAnd(SimpleNode node)
    {
        node.children().toList().forEach(Simplifier::simplifyBoolean);
        for (SimpleNode child : node.children().toList())
        {
            String childStr = child.format().trim();
            if (childStr.equals("FALSE"))
            {
                node.replaceWith(child);
                return;
            }
            if (childStr.equals("TRUE") && node.jjtGetNumChildren() > 1)
            {
                child.remove();
            }
        }
        if (node.jjtGetNumChildren() == 1)
        {
            node.replaceWith(node.child(0));
        }
    }
}
