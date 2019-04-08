package com.developmentontheedge.be5.base	.model.groovy;

public class PropertyAccessHelper
{

    private static final String PROPERTY_PREFIX = "_";
    private static final String VALUE_PREFIX = "$";

    public static boolean isPropertyAccess(String v)
    {
        return v != null && v.startsWith(PROPERTY_PREFIX);
    }

    public static boolean isValueAccess(String v)
    {
        return v != null && (v.startsWith(VALUE_PREFIX));
    }
}
