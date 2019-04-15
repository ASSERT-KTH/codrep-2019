package com.developmentontheedge.sql.model;

/**
 * Predefined functions and operators.
 */
public class BeMacroFunction implements Function
{
    public BeMacroFunction(String name, int numberOfParameters)
    {
        this.name = name;
        this.priority = Function.FUNCTION_PRIORITY;
        this.minNumberOfParams = numberOfParameters;
        this.maxNumberOfParams = numberOfParameters;
    }

    public BeMacroFunction(String name, int minNumberOfParams, int maxNumberOfParams)
    {
        this.name = name;
        this.priority = Function.FUNCTION_PRIORITY;
        this.minNumberOfParams = minNumberOfParams;
        this.maxNumberOfParams = maxNumberOfParams;
    }

    /////////////////////////////////////////////////////////////////
    // Properties
    //

    private final String name;
    private final int priority;
    private final int minNumberOfParams;
    private final int maxNumberOfParams;

    /**
     * Returns the name of the node (operator symbol or function name).
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns the function or operator priority.
     */
    @Override
    public int getPriority()
    {
        return priority;
    }

    /**
     * Returns the lowest possible number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    @Override
    public int minNumberOfParams()
    {
        return minNumberOfParams;
    }

    /**
     * Returns the biggest possible number of required parameters, or -1 if any number of
     * parameters is allowed.
     */
    @Override
    public int maxNumberOfParams()
    {
        return maxNumberOfParams;
    }

    private AstBeMacro macro;

    public void setMacro(AstBeMacro macro)
    {
        this.macro = macro;
    }

    public AstBeMacro getMacro()
    {
        return macro;
    }

    public SimpleNode getReplacement(AstFunNode node)
    {
        int i = 0;
        AstBeMacroArgumentList argumentList = (AstBeMacroArgumentList) macro.child(0);
        SimpleNode substitution = macro.child(1).clone();
        for (AstIdentifierConstant arg : argumentList.children().select(AstIdentifierConstant.class))
        {
            SimpleNode replacement = i < node.jjtGetNumChildren() ? node.child(i++)
                    :argumentList.child(argumentList.indexOf(arg) + 1);
            replace(substitution, arg, replacement);
        }
        return substitution;
    }

    public void replace(SimpleNode node, AstIdentifierConstant target, SimpleNode replacement)
    {
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            SimpleNode child = node.child(i);
            replace(child, target, replacement);
            if (child instanceof AstIdentifierConstant && ((AstIdentifierConstant) child).getValue().equals(target.getValue()))
                child.replaceWith(replacement.clone());
        }
    }
}
