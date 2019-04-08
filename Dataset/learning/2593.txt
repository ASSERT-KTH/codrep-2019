package com.developmentontheedge.sql.model;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public abstract 	class AbstractParser implements Parser
{
    protected ParserContext context = DefaultParserContext.getInstance();

    @Override
    public ParserContext getContext()
    {
        return context;
    }

    @Override
    public void setContext(ParserContext context)
    {
        this.context = context;
    }

    ///////////////////////////////////////////////////////////////////
    // Error processing issues
    //

    protected int status;
    protected ArrayList<String> messages = new ArrayList<>();

    public void warning(String warningStr)
    {
        messages.add("Warning: " + warningStr + ".");
        status |= STATUS_WARNING;
    }

    public void error(String errorStr)
    {
        messages.add("Error: " + errorStr + ".");
        status |= STATUS_ERROR;
    }

    @Override
    public List<String> getMessages()
    {
        return messages;
    }

    ///////////////////////////////////////////////////////////////////
    // Utility functions
    //

    protected AstStart astStart;
    protected boolean squashed;
    private Function fn;

    private void squash()
    {
        if (squashed)
            return;
        astStart.tree().filter(Squasheable.class::isInstance)
                .remove(node -> node.jjtGetParent() instanceof AstBeNode)
                .filter(node -> node.jjtGetNumChildren() == 1)
                .forEach(node -> {
                    SimpleNode child = node.child(0);
                    if (child.specialPrefix == null)
                        child.specialPrefix = node.specialPrefix;
                    if (child.specialSuffix == null)
                        child.specialSuffix = node.specialSuffix;
                    node.replaceWith(child);
                });
    }

    @Override
    public AstStart getStartNode()
    {
        squash();
        return astStart;
    }

    @Override
    public int parse(String expression)
    {
        reinit();

        try
        {
            ReInit(new StringReader(expression));
            astStart = Start();
        }
        catch (Throwable t)
        {
            error(t.toString());
            astStart = new AstStart(SqlParserTreeConstants.JJTSTART);
        }

        astStart.setStatus(status);
        astStart.setMessages(messages);

        return status;
    }

    public abstract void ReInit(Reader reader);

    public abstract AstStart Start() throws ParseException;

    protected void reinit()
    {
        status = 0;
        messages.clear();
        astStart = null;
        squashed = false;
    }

    /**
     * Set ups build in operators.
     *
     * @pending validation.
     */
    protected void setMacro(String name, int minNumberOfParams, int maxNumberOfParams, AstBeMacro node)
    {
        BeMacroFunction op = new BeMacroFunction(name, minNumberOfParams, maxNumberOfParams);
        context.declareFunction(op);
        op.setMacro(node);
    }

    protected void setDbmsTransform(AstBeDbmsTransform node, String name, int numberOfParams)
    {
        DbSpecificFunction op = new DbSpecificFunction(name, numberOfParams);
        context.declareFunction(op);
        node.setFunction(op);
    }

    protected void setOperator(AstFunNode node, String name)
    {
        Function operator = context.getFunction(name.trim());

        if (operator == null && getMode() == Mode.DBMS_TRANSFORM)
        {
            operator = new DbSpecificFunction(name, -1);
            context.declareFunction(operator);
            node.setWithinDbmsTransform(true);
        }
        else if (operator == null)
        {
            error("Unknown operator '" + name + "'");
            operator = new UndeclaredFunction(name, -1);
        }

        AstFunNode funNode = node;
        funNode.setFunction(operator);
    }

    protected void setOperator(AstFunNode node, Token op)
    {
        node.setOperator(op);
        setOperator(node, op.image);
    }

    protected void setFunction(Function fn)
    {
        this.fn = fn;
    }

    protected Function getFunction()
    {
        return fn;
    }

    private final Deque<Mode> modes = new ArrayDeque<>(Arrays.asList(Mode.DEFAULT));

    public Mode getMode()
    {
        return modes.peek();
    }

    public void pushMode(Mode mode)
    {
        modes.push(mode);
    }

    public void popMode(Mode mode)
    {
        Mode curMode = modes.poll();
        if (mode != curMode)
            throw new InternalError("Invalid mode: " + curMode + " (expected: " + mode + ")");
    }

    public boolean popModeOptional(Mode mode)
    {
        if (modes.peek() != mode)
            return false;
        modes.poll();
        return true;
    }
}