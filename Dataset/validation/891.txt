package com.developmentontheedge.sql.format;


public class SqlTypeUtils
{
    public static boolean isNumber(Class<?> type)
    {
        return type == Long.class ||
                type == Integer.class ||
                type == Short.class ||
                type == Double.class ||
                type == Float.class;
    }

    public static boolean isNumber(String className)
    {
        return "java.lang.Long".equals(className) ||
                "java.lang.Integer".equals(className) ||
                "java.lang.Short".equals(className) ||
                "java.lang.Double".equals(className) ||
                "java.lang.Float".equals(className);
    }

    public static Object parseValue(Object value, String className)
    {
        if (value.getClass() == String.class)
        {
            if ("java.lang.Long".equals(className))
            {
                return Long.valueOf((String) value);
            }
            else if ("java.lang.Integer".equals(className))
            {
                return Integer.valueOf((String) value);
            }
            if ("java.lang.Short".equals(className))
            {
                return Short.valueOf((String) value);
            }
            else if ("java.lang.Double".equals(className))
            {
                return 
Double.valueOf((String) value);
            }
            if ("java.lang.Float".equals(className))
            {
                return Float.valueOf((String) value);
            }
        }

        return value;
    }

}
