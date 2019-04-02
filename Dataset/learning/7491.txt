package com.developmentontheedge.be5.base.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils
{
    private static final int DATE_PARSING_MODE_DATE = 0;
    private static final int DATE_PARSING_MODE_TIME = 1;
    private static final int DATE_PARSING_MODE_DATETIME = 2;

    private static Locale[] POPULAR_LOCALES = new Locale[]
            {
                    Locale.US,
                    new Locale("ru_RU"),
                    Locale.UK,
                    Locale.CANADA,
                    Locale.ENGLISH,
                    Locale.FRANCE,
                    Locale.FRENCH,
                    Locale.GERMAN,
                    Locale.GERMANY,
                    Locale.ITALIAN,
                    Locale.ITALY,
                    Locale.JAPAN,
                    Locale.JAPANESE
            };

    private static final String[] dateFormats = new String[]{"yyyy-MM-dd"};
    private static final String[] timeFormats = new String[]{"HH:mm:ss"};
    private static final String[] dateTimeFormats = new String[]{"yyyy-MM-dd HH:mm:ss"};

    public static String inClause(int count)
    {
        if (count <= 0)
        {
            throw Be5Exception.internal("Error in function inClause(int), count value: " + count + ", must be > 0");
        }
        return "(" + IntStream.range(0, count).mapToObj(x -> "?").collect(Collectors.joining(", ")) + ")";
    }

    public static String[] addPrefix(String prefix, Object[] values)
    {
        String[] withPrefix = new String[values.length];
        for (int i = 0; i < values.length; i++)
        {
            withPrefix[i] = prefix + values[i];
        }

        return withPrefix;
    }

    /**
     * Check given object for an empty value.
     * <br/>Value is empty, if it equals null or it is instance of type String and it's value doesn't have any symbols, except spaces.
     *
     * @param value value
     * @return returns true, if value is empty, otherwise value is false
     */
    public static boolean isEmpty(Object value)
    {
        if (value == null)
        {
            return true;
        }
        if (value instanceof String && ((String) value).trim().isEmpty())
        {
            return true;
        }
        if (value instanceof Object[] && ((Object[]) value).length == 0)
        {
            return true;
        }
        if (value instanceof Collection && ((Collection) value).isEmpty())
        {
            return true;
        }
        return false;
    }

    public static void requireNonEmpty(Object value)
    {
        requireNonEmpty(value, "Required not empty");
    }

    public static void requireNonEmpty(Object value, String errorMessage)
    {
        if (isEmpty(value))
        {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] changeTypes(Object[] values, Class<T> aClass)
    {
        T[] changeType = (T[]) Utils.changeType(values, getArrayClass(aClass));
        if (changeType == null && values != null)
        {
            return (T[]) new Object[0];
        }
        return changeType;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T[]> getArrayClass(Class<T> clazz)
    {
        return (Class<? extends T[]>) Array.newInstance(clazz, 0).getClass();
    }

    //todo parametrize: <T> T changeType( Object val, T valClass )
    public static Object changeArrayItemTypes(Object[] val, Class valClass)
    {
        Object[] vals = (Object[]) val;
        if (vals.length == 0)
        {
            return null;
        }
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < vals.length; i++)
        {
            Object newVal = changeType(vals[i], valClass.getComponentType());
            //when submitted String array cannot be converted to Timestamp array, it may mean
            //that although the column type is Timestamp, it should be handled as java.sql.Date type
            //we just return the _val_ unchanged, it will be checked in addRecordFilter() and there will
            // be an attempt to parse it as Date
            if (java.sql.Timestamp.class.equals(valClass.getComponentType()) && newVal.equals(vals[i]))
            {
                return val;
            }

            out.add(newVal);
        }
        return out.toArray((Object[]) Array.newInstance(valClass.getComponentType(), 0));
    }

    public static Object parseType(String val, Class valClass)
    {
        if ("".equals(val)) return null;

        if (Double.class.equals(valClass) || double.class.equals(valClass))
        {
            return Double.valueOf(fixNumber(val, false));
        }
        if (Float.class.equals(valClass) || float.class.equals(valClass))
        {
            return Float.valueOf(fixNumber(val, false));
        }
        if (Byte.class.equals(valClass) || byte.class.equals(valClass))
        {
            return Byte.valueOf(fixNumber(val, true));
        }
        if (Short.class.equals(valClass) || short.class.equals(valClass))
        {
            return Short.valueOf(fixNumber(val, true));
        }
        if (Integer.class.equals(valClass) || int.class.equals(valClass))
        {
            return Integer.valueOf(fixNumber(val, true));
        }
        if (Long.class.equals(valClass) || long.class.equals(valClass))
        {
            return Long.valueOf(fixNumber(val, true));
        }
        if (Boolean.class.equals(valClass) || boolean.class.equals(valClass))
        {
            String s = (val).toLowerCase();
            return "true".equals(s) || "on".equals(s) ||
                    "yes".equals(s) || "1".equals(s);
        }

        if (BigDecimal.class.equals(valClass))
        {
            return new BigDecimal(fixNumber(val, false));
        }
        if (BigInteger.class.equals(valClass))
        {
            return new BigInteger(fixNumber(val, false));
        }

        if (File.class.equals(valClass))
        {
            return new File(val);
        }

        return parseDateOrTimeType(val, valClass);
    }

    private static Object parseDateOrTimeType(String val, Class valClass)
    {
        try
        {
            if (java.util.Date.class.equals(valClass))
            {
                DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT);
                df.setLenient(false);
                java.util.Date parsed;
                try
                {
                    parsed = df.parse(val);
                }
                catch (ParseException pe)
                {
                    parsed = parseDateWithOtherLocales(val, DATE_PARSING_MODE_DATE);
                }
                return parsed;
            }
            if (java.sql.Date.class.equals(valClass))
            {
                DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT);
                df.setLenient(false);
                java.util.Date parsed;
                try
                {
                    parsed = df.parse(val);
                }
                catch (ParseException pe)
                {
                    parsed = parseDateWithOtherLocales(val, DATE_PARSING_MODE_DATE);
                }

                return new java.sql.Date(parsed.getTime());
            }
            if (java.sql.Time.class.equals(valClass))
            {
                DateFormat df = DateFormat.getTimeInstance(DateFormat.DEFAULT);
                df.setLenient(false);
                java.util.Date parsed;
                try
                {
                    parsed = df.parse(val);
                }
                catch (ParseException pe)
                {
                    parsed = parseDateWithOtherLocales(val, DATE_PARSING_MODE_TIME);
                }

                return new java.sql.Time(parsed.getTime());
            }
            if (java.sql.Timestamp.class.equals(valClass))
            {
                String str = val;

                boolean isHtml5 = str.length() == 16 && str.charAt(10) == 'T';
                if (isHtml5)
                {
                    val = Utils.subst(str, "T", " ") + ":00";
                }

                java.util.Date parsed;
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
                df.setLenient(false);
                try
                {
                    parsed = df.parse(val);
                }
                catch (ParseException pe)
                {
                    try
                    {
                        parsed = parseDateWithOtherLocales(val, DATE_PARSING_MODE_DATETIME);
                    }
                    catch (ParseException pe2)
                    {
                        df = DateFormat.getDateInstance(DateFormat.DEFAULT);
                        try
                        {
                            parsed = df.parse(val);
                        }
                        catch (ParseException pe3)
                        {
                            parsed = parseDateWithOtherLocales(val, DATE_PARSING_MODE_DATE);
                        }
                    }
                }

                return new java.sql.Timestamp(parsed.getTime());
            }
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }

        throw new IllegalArgumentException();
    }

    public static Object changeType(Object val, Class valClass)
    {
        if (val == null)
        {
            return null;
        }

        if (val.getClass().equals(valClass))
        {
            return val;
        }

        if (val.getClass().isArray() && valClass.isArray() &&
                !val.getClass().getComponentType().equals(valClass.getComponentType()))
        {
            return changeArrayItemTypes((Object[]) val, valClass);
        }

        if (java.sql.Timestamp.class.equals(valClass) || java.util.Date.class.equals(valClass) ||
                XMLGregorianCalendar.class.equals(valClass))
        {
            return changeDateType(val, valClass);
        }

        if (val instanceof Boolean && String.class.equals(valClass))
        
{
            return val.toString();
        }

        if (val instanceof Number)
        {
            if (String.class.equals(valClass))
            {
                return val.toString();
            }
            if (int.class.equals(valClass) || Integer.class.equals(valClass))
            {
                return ((Number) val).intValue();
            }
            if (long.class.equals(valClass) || Long.class.equals(valClass))
            {
                return ((Number) val).longValue();
            }
            if (BigDecimal.class.equals(valClass))
            {
                return new BigDecimal(((Number) val).doubleValue());
            }
        }

        if (val instanceof String)
        {
            return parseType((String) val, valClass);
        }
        else
        {
            return val;
        }
    }

    private static Object changeDateType(Object val, Class valClass)
    {
        if (java.sql.Timestamp.class.equals(valClass))
        {
            if (val instanceof java.util.Date)
            {
                return new java.sql.Timestamp(((java.util.Date) val).getTime());
            }
            if (val instanceof java.util.Calendar)
            {
                return new java.sql.Timestamp(((java.util.Calendar) val).getTime().getTime());
            }
        }

        if (java.util.Date.class.equals(valClass))
        {
            if (val instanceof java.sql.Timestamp)
            {
                return new java.util.Date(((java.sql.Timestamp) val).getTime());
            }
            if (val instanceof java.util.Date)
            {
                return new java.sql.Date(((java.util.Date) val).getTime());
            }
            if (val instanceof java.util.Calendar)
            {
                return new java.sql.Date(((java.util.Calendar) val).getTime().getTime());
            }
            if (val instanceof XMLGregorianCalendar)
            {
                XMLGregorianCalendar xcal = (XMLGregorianCalendar) val;
                return xcal.toGregorianCalendar().getTime();
            }
        }

        if (XMLGregorianCalendar.class.equals(valClass))
        {
            if (val instanceof java.util.Date)
            {
                try
                {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime((java.util.Date) val);
                    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
                }
                catch (DatatypeConfigurationException e)
                {
                    throw new IllegalArgumentException(e);
                }
            }
            if (val instanceof String)
            {
                try
                {
                    return DatatypeConverter.parseDateTime((String) val);
                }
                catch (IllegalArgumentException e)
                {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        return parseDateOrTimeType((String) val, valClass);
    }


    /**
     * Parse date using popular locales.
     * When failed, throws ParseException.
     * It is needed to try all locales when the date could not
     * be parsed with user's locale.
     *
     * @param val
     * @return parsed date
     * @throws ParseException when none of the locales helps.
     */
    private static Date parseDateWithOtherLocales(String val, int parsingMode) throws ParseException
    {
        ParseException lastException = null;
        for (int i = 0; i < POPULAR_LOCALES.length; i++)
        {
            try
            {
                DateFormat df = null;
                //dates and times are handled differently
                switch (parsingMode)
                {
                    case DATE_PARSING_MODE_DATE:
                        df = DateFormat.getDateInstance(DateFormat.DEFAULT, POPULAR_LOCALES[i]);
                        break;
                    case DATE_PARSING_MODE_TIME:
                        df = DateFormat.getTimeInstance(DateFormat.DEFAULT, POPULAR_LOCALES[i]);
                        break;
                    case DATE_PARSING_MODE_DATETIME:
                        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, POPULAR_LOCALES[i]);
                        break;
                    default:
                        df = DateFormat.getDateInstance(DateFormat.DEFAULT, POPULAR_LOCALES[i]);
                }
                //return when parsed successfully
                java.util.Date parsed = df.parse(val);
                return parsed;
            }
            catch (ParseException pe)
            {
                //could not parse, continue with other locales.
                lastException = pe;
            }
        }

        //now try other specific date/time patterns
        String[] formats = null;
        switch (parsingMode)
        {
            case DATE_PARSING_MODE_DATE:
                formats = dateFormats;
                break;
            case DATE_PARSING_MODE_TIME:
                formats = timeFormats;
                break;
            case DATE_PARSING_MODE_DATETIME:
                formats = dateTimeFormats;
                break;
            default:
                formats = dateFormats;
        }

        for (int i = 0; i < formats.length; i++)
        {
            String pattern = formats[i];
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            try
            {
                return sdf.parse(val);
            }
            catch (ParseException pe)
            {
                //could not parse, continue with other patterns.
                lastException = pe;
            }
        }

        //the string was not parsed, throw an exception.
        throw lastException;
    }

    public static String fixNumber(String number, boolean isInt)
    {
        if (number == null)
        {
            return null;
        }
        number = subst(number, " ", "");
        int pointInd = number.lastIndexOf('.');
        int commaInd = number.lastIndexOf(',');

        if (commaInd > 0 && pointInd > commaInd)
        {
            return subst(number, ",", "");
        }
        if (!isInt && commaInd > 0)
        {
            return subst(number, ",", ".");
        }

        if (isInt)
        {
            try
            {
                Long.valueOf(number);
            }
            catch (NumberFormatException nfe)
            {
                if (number.endsWith(".0"))
                {
                    return subst(number, ".0", "");
                }
            }
        }

        return number;
    }

    /**
     * Try`s array values as an array of value pair and converts it to the Map, where first value is the key.
     *
     * @param values Object[n][2]
     * @return value Map
     */
    public static Map valueMap(Object[][] values)
    {
        if (values == null)
        {
            return null;
        }
        LinkedHashMap map = new LinkedHashMap(values.length);
        for (int i = 0; i < values.length; i++)
        {
            map.put(values[i][0], values[i][1]);
        }
        return map;
    }

    public static Map valueMap(Object... values)
    {
        return SimpleCompositeMap.valueMap(values);
    }

    public static Map valueNotNullMap(Object... values)
    {
        return SimpleCompositeMap.valueNotNullMap(values);
    }

    /**
     * Substitute string "fromText" in "text" for string "toText".
     * Substituted text will be returned as a result.
     *
     * @param text     text, where fromText is substituting for another text.
     * @param fromText text for substituting
     * @param toText   text, that is substituting fromText
     * @return returns substituted text
     */
    public static String subst(String text, String fromText, String toText)
    {
        return subst(text, fromText, toText, "");
    }

    /**
     * Substitute string "fromText" in "text" for another string.
     * Substitution string is "toText" or, if "toText" is empty (isEmpty), then "defText".
     * Substituted text will be returned as a result.
     *
     * @param text     text, where fromText is substituting for another text.
     * @param fromText text for substituting
     * @param toText   text, that is substituting fromText
     * @param defText  text, that is substituting fromText, if "toText" is empty (isEmpty)
     * @return returns substituted text
     */
    public static String subst(String text, String fromText, String toText, String defText)
    {
        if (text == null)
        {
            return null;
        }
        int prevPos = 0;
        String newText = toText == null || "".equals(toText) ? defText : toText;
        for (int pos = text.indexOf(fromText, prevPos); pos >= 0;
             pos = text.indexOf(fromText, prevPos + newText.length()))
        {
            prevPos = pos;
            text = new StringBuffer(text).replace(pos, pos + fromText.length(), newText).toString();
        }
        return text;
    }

    public static <T> T ifNull(Object val, T def)
    {
        if (val != null)
        {
            return (T) val;
        }
        return def;
    }
//
//    public static boolean isSystemDeveloperORDevMode()
//    {
//        return UserInfoHolder.isSystemDeveloper() || ModuleLoader2.getPathsToProjectsToHotReload().size() > 0;
//    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }

    /**
     * Generates random password, containing 8 symbols using english alphabet and numbers.
     *
     * @return generated password
     */
    public static char[] newRandomPassword()
    {
        return newRandomPassword("abcdefghijklmnopqrstuvwxyz0123456789");
    }

    /**
     * Generates random password, containing 8 symbols from specified symbols array.
     *
     * @param pool symbols to use in password
     * @return generated password
     */
    public static char[] newRandomPassword(String pool)
    {
        try
        {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            char[] pass = new char[8];
            for (int i = 0; i < 8; i++)
            {
                pass[i] = pool.charAt(sr.nextInt(pool.length()));
            }
            return pass;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Rewrites specified russian string with translit. Russian string is attempting to be in "KOI8-R" encoding.
     */
    public static String translit(String val)
    {
        if (val == null)
        {
            return null;
        }
        try
        {
            val = val.replace("\u2116", "N");
            byte[] bytes = val.getBytes("KOI8-R");
            for (int i = 0; i < bytes.length; i++)
            {
                byte old = bytes[i];
                bytes[i] &= 0x7f;
                if (old == bytes[i])
                {
                    continue;
                }

                // inverse case since KOI8-R has it inverted
                if (bytes[i] >= 0x41 && bytes[i] <= 0x5A)
                {
                    bytes[i] += 0x20;
                }
                else if (bytes[i] >= 0x61 && bytes[i] <= 0x7A)
                {
                    bytes[i] -= 0x20;
                }
            }
            String ret = new String(bytes);
            ret = ret.replace('\\', 'e').replace("\"", "'")
                    .replace("~", "CH")
                    .replace("{", "SH")
                    .replace("}", "SHH")
                    .replace("", "_")
                    .replace("|", "E")
                    .replace("`", "JU")
                    .replace("#v", "jo")
                    .replace("^", "ch")
                    .replace("[", "sh")
                    .replace("]", "shh")
                    .replace("@", "ju")
                    .replace("3V", "JO");
            return ret;
        }
        catch (UnsupportedEncodingException exc)
        {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Replace all occurrences of bracketed by the specified opener and closer expressions for the specified replacement string.
     *
     * @param html        text for substitution
     * @param opener      open bracket
     * @param closer      close bracket
     * @param replacement replacement string, can be null
     * @return prepared string
     */
    public static String removeBracketed(String html, String opener, String closer, String replacement)
    {
        if (html == null)
        {
            return null;
        }
        StringBuilder clearHtml = new StringBuilder(html);
        int start = 0;
        int openerLength = opener.length();
        int closerLength = closer.length();
        int replacementLength = replacement != null ? replacement.length() : 0;
        while ((start = clearHtml.indexOf(opener, start)) >= 0)
        {
            int end = clearHtml.indexOf(closer, start + openerLength);
            if (end == -1)
            {
                break; // malformed html
            }
            if (replacement != null)
            {
                clearHtml.replace(start, end + closerLength, replacement);
            }
            else
            {
                clearHtml.delete(start, end + closerLength);
            }
            start = start + replacementLength;
        }
        return clearHtml.toString();
    }

    private static Map<String, String> entitiesConversion = new LinkedHashMap<>();
    private static Map<String, String> entitiesBackwardConversion = new LinkedHashMap<>();

    static
    {
        String[][] convTable = new String[][]
                {
                        {"&amp;", "&"},
                        {"&quot;", "\""},
                        {"&lt;", "<"},
                        {"&gt;", ">"},
                        {"&nbsp;", " "},
                        {"&ndash;", "\u2013"},
                        {"&mdash;", "\u2014"},
                        {"&laquo;", "\u00AB"},
                        {"&raquo;", "\u00BB"},
                };
        for (String[] sArr : convTable)
        {
            entitiesConversion.put(sArr[0], sArr[1]);
            entitiesBackwardConversion.put(sArr[1], sArr[0]);
        }
        entitiesBackwardConversion.remove(" ");
    }

    /**
     * Replaces in the string one expressions to another:
     * "&quot;" is replacement for "\""
     * "&lt;" is replacement for "<"
     * "&gt;" is replacement for ">"
     * "&nbsp;" is replacement for " "
     * "&amp;" is replacement for "&"
     *
     * @param xml text for replacing
     * @return prepared string
     */
    public static String replaceXmlEntities(String xml)
    {
        return replaceXmlEntities(xml, entitiesConversion);
    }

    /**
     * Replaces in the string one expressions to another:
     * "\"" is replacement for "&quot;"
     * "<" is replacement for "&lt;"
     * ">" is replacement for "&gt;"
     * "&" is replacement for "&amp;"
     *
     * @param inText text for replacing
     * @return prepared string
     */
    public static String safeXML(String inText)
    {
        return replaceXmlEntities(inText, entitiesBackwardConversion);
    }

    private static String replaceXmlEntities(String xml, Map<String, String> replaceMap)
    {
        if (xml == null)
        {
            return null;
        }
        StringBuilder text = new StringBuilder(xml);
        for (Map.Entry<String, String> entry : replaceMap.entrySet())
        {
            String repl = entry.getKey();
            String replTo = entry.getValue();
            int replLength = repl.length();
            int replToLength = replTo.length();
            int pos = text.indexOf(repl);
            while (pos != -1)
            {
                text.replace(pos, pos + replLength, replTo);
                pos = text.indexOf(repl, pos + replToLength);
            }
        }
        return text.toString();
    }
}
