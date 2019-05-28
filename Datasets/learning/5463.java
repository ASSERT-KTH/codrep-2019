package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstFunNode;
import com.developmentontheedge.sql.model.BeMacroFunction;
import com.developmentontheedge.sql.model.SimpleNode;

public class 
MacroExpander
{
    public void expandMacros(SimpleNode node)
    {
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            SimpleNode child = node.child(i);
            expandMacros(child);
            if ((child instanceof AstFunNode) && ((AstFunNode) child).getFunction() instanceof BeMacroFunction)
            {
                transformMacroFunction((AstFunNode) child);
            }
        }
    }

    private void transformMacroFunction(AstFunNode node)
    {
        BeMacroFunction function = (BeMacroFunction) node.getFunction();
        SimpleNode replacement = function.getReplacement(node);
        expandMacros(replacement);
        node.replaceWith(replacement);
    }
}
