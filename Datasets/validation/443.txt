package com.developmentontheedge.sql.model;

import one.util.streamex.EntryStream;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AstBeNode extends SimpleNode
{
    protected String tagName;
    protected Set<String> allowedParameters = Collections.emptySet();
    protected Map<String, String> parameters = new LinkedHashMap<>();

    public AstBeNode(int i)
    {
        super(i);
    }

    public void init()
    {
        AstBeParameterList list = this.children().select(AstBeParameterList.class).findFirst().get();
        for (int i = 0; i < list.jjtGetNumChildren() - 1; i++)
        {
            addParameter(list.child(i).format().trim(), list.child(++i).format().trim());
        }
        this.removeChild(list);
    }

    public void addParameter(String key, String value)
    {
        String oldValue = parameters.get(key);
        if (oldValue != null)
        {
            throw new IllegalArgumentException("BE tag <" + tagName + ">: attribute: '" + key + "' is specified twice");
        }
        setParameter(key, value);
    }

    public void setParameter(String key, String value)
    {
        Objects.requireNonNull(value);
        if (allowedParameters != null && !allowedParameters.contains(key))
        {
            throw new IllegalArgumentException("BE tag <" + tagName + ">: unsupported attribute: '" + key + "'");
        }
        if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))
            value = value.substring(1, value.length() - 1);
        parameters.put(key, value);
    }

    public String getParameter(String key)
    {
        return parameters.get(key);
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public String getParametersString()
    {
        String result = EntryStream.of(parameters).mapKeyValue((k, v) -> k + "=\"" + v + "\"").joining(" ", " ", "");
        return result.equals(" ") ? "" : result;
    }

    private String start()
    {
        return "<";
    }

    private String end()
    {
        return ">";
    }

    @Override
    protected void formatBody(StringBuilder sb, Set<Token> printedSpecial)
    {
        if (tagName == null)
        {
            super.formatBody(sb, printedSpecial);
            return;
        }
        append(sb, start() + tagName);
        append(sb, getParametersString());
        // TODO: support <sql ... />
        if (children.isEmpty() && !tagName.equals("sql") && !tagName.equals("else"))
            append(sb, "/" + end());
        else
        {
            sb.append(">");
            SimpleNode prev = null;
            for (SimpleNode child : children)
            {
                if (prev != null)
                {
                    append(sb, getChildrenDelimiter(prev, child));
                }
                prev = child;
                child.format(sb, printedSpecial);
            }
            append(sb, "</" + tagName + end());
        }
    }

    @Override
    public SimpleNode clone()
    {
        AstBeNode clone = 
(AstBeNode) super.clone();
        clone.parameters = new LinkedHashMap<>(clone.parameters);
        return clone;
    }
}