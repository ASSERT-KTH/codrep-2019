// $Id: ExprEvaluator.java,v 1.3 2014/02/07 07:24:38 lan Exp $
package com.developmentontheedge.sql.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates expressions like par==val||par2==val2, par||par2||par3||par4==val4
 * Used in Utils.handleConditionalParts()
 */
	public class ExprEvaluator
{
    Map<String, String> values;
    String expr;

    private static final Pattern equalsPattern = Pattern.compile("([\\w-]+)==([\\w-]+)");
    private static final Pattern notEqualsPattern = Pattern.compile("([\\w-]+)!=([\\w-]+)");
    private static final Pattern notExistsPattern = Pattern.compile("!([\\w-]+)");
    private static final Pattern existPattern = Pattern.compile("([\\w-]+)");

    public ExprEvaluator(Map<String, String> values, String expr)
    {
        this.values = values;
        this.expr = expr;
    }

    public boolean evaluateOR()
    {
        String[] parts = expr.split("\\|\\|");
        for (int i = 0; i < parts.length; i++)
        {
            Matcher equalsMatcher = equalsPattern.matcher(parts[i]);
            Matcher notEqualsMatcher = notEqualsPattern.matcher(parts[i]);
            Matcher notExistsMatcher = notExistsPattern.matcher(parts[i]);
            Matcher existMatcher = existPattern.matcher(parts[i]);
            if (equalsMatcher.find())
            {
                String paramName = equalsMatcher.group(1);
                String val = equalsMatcher.group(2);
                if (val.equals(values.get(paramName)))
                {
                    return true;
                }
            }
            else if (notEqualsMatcher.find())
            {
                String paramName = notEqualsMatcher.group(1);
                String val = notEqualsMatcher.group(2);
                if (values.get(paramName) != null && !val.equals(values.get(paramName)))
                {
                    return true;
                }
            }
            else if (notExistsMatcher.matches())
            {
                String paramName = notExistsMatcher.group(1);
                if (values.get(paramName) == null)
                {
                    return true;
                }
            }
            else if (existMatcher.matches())
            {
                String paramName = existMatcher.group(1);
                if (values.get(paramName) != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean evaluateAND()
    {
        String[] parts = expr.split("&&");
        for (String part : parts)
        {
            Matcher equalsMatcher = equalsPattern.matcher(part);
            Matcher notEqualsMatcher = notEqualsPattern.matcher(part);
            Matcher notExistsMatcher = notExistsPattern.matcher(part);
            Matcher existMatcher = existPattern.matcher(part);
            if (equalsMatcher.find())
            {
                String paramName = equalsMatcher.group(1);
                String val = equalsMatcher.group(2);
                if (!val.equals(values.get(paramName)))
                {
                    return false;
                }
            }
            else if (notEqualsMatcher.find())
            {
                String paramName = notEqualsMatcher.group(1);
                String val = notEqualsMatcher.group(2);
                if (!(values.get(paramName) != null && !val.equals(values.get(paramName))))
                {
                    return false;
                }
            }
            else if (notExistsMatcher.matches())
            {
                String paramName = notExistsMatcher.group(1);
                if (values.get(paramName) != null)
                {
                    return false;
                }
            }
            else if (existMatcher.matches())
            {
                String paramName = existMatcher.group(1);
                if (values.get(paramName) == null)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean evaluate()
    {
        String[] parts = expr.split("&&");
        return parts.length > 1 ? evaluateAND() : evaluateOR();
    }
}
