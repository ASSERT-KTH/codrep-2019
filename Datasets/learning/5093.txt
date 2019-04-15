package com.developmentontheedge.be5.base.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An utility class.
 *
 * @author asko
 * @see String
 * @see Objects
 */
public class MoreStrings
{

    /**
     * An utility class, not intended to be instantiated.
     */
    private MoreStrings()
    {
        throw new AssertionError();
    }

    /**
     * Tests if this string starts with any of the specified prefixes.
     */
    public static boolean startsWithAny(String str, String... prefixes)
    {
        checkNotNull(str);
        checkNotNull(prefixes);

        return Stream.of(prefixes).anyMatch(prefix -> str.startsWith(prefix));
    }

    /**
     * Returns an empty string if the given value is null or calls {@link Object#toString()} otherwise.
     *
     * @see Objects#toString(Object, String)
     */
    public static String valueOfNullable(Object value)
    {
        return value != null ? value.toString() : "";
    }

    /**
     * Replaces each substring of this string that matches the pattern with the evaluated replacement.
     */
    public static String replace(String string, Pattern pattern, Function<Matcher, String> evaluateReplacement)
    {
        checkNotNull(string);
        checkNotNull(pattern);
        checkNotNull(evaluateReplacement);

        Matcher matcher = pattern.matcher(string);
        StringBuilder sb = new StringBuilder();
        int start;

        for (start = 0; matcher.find(start); start = matcher.end())
        {
            sb.append(string.substring(start, matcher.start()));
            sb.append(evaluateReplacement.apply(matcher));
        }

        sb.append(string.substring(start));

        return sb.toString();
    }

    /**
     * Replaces each substring of this string that matches the pattern with the evaluated replacement.
     * The given pattern must contain at least one group, that will be used as variable's name.
     *
     * @see MoreStrings#variablePattern(String, String)
     */
    public static String substituteVariables(String string, Pattern pattern, final Function<String, String> evaluateVariable)
    {
        checkNotNull(string);
        checkNotNull(pattern);
        checkNotNull(evaluateVariable);
        return replace(string, pattern, new Function<Matcher, String>()
        {
            @Override
            public String apply(Matcher m)
            {
                checkArgument (m.groupCount() >= 1);
                return evaluateVariable.apply(m.group(1));
            }
        });
    }

    /**
     * Creates a pattern for a placeholder, e.g.
     * <pre>
     * <code>
     *   pattern = MoreStrings.variablePattern("${", "}");
     *   evaluated = MoreStrings.substituteVariables("Hi, ${name}!", pattern, evaluator::eval);
     * </code>
     * </pre>
     *
     * @see MoreStrings#substituteVariables(String, Pattern, Function)
     */
    public static Pattern variablePattern(String prefix, String suffix)
    {
        checkNotNull(prefix);
        checkNotNull(suffix);
        return Pattern.compile(Pattern.quote(prefix) + "(.*?)" + Pattern.quote(suffix));
    }

    public static String decodeUrl(String url)
    {
        try
        {
            return URLDecoder.decode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw Be5Exception.internal(e);
        }
    }

    public static String encodeUrl(String url)
    {
        try
        {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
