package com.developmentontheedge.be5.base.model.groovy;

import com.developmentontheedge.be5.base.util.Utils;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import org.codehaus.groovy.runtime.GStringImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class DynamicPropertyUtils
{
    private static final Logger log = Logger.getLogger(DynamicPropertyUtils.class.getName());

    static final Map<String, String> beanInfoConstants = new HashMap<>();

    static
    {
        Field[] fields = BeanInfoConstants.class.getDeclaredFields();
        for (Field f : fields)
        {
            if (Modifier.isStatic(f.getModifiers()))
            {
                try
                {
                    beanInfoConstants.put(f.getName(),
                            BeanInfoConstants.class.getDeclaredField(f.getName()).get(null).toString());
                }
                catch (Exception exc)
                {
                    throw new RuntimeException(exc);
                }
            }
        }
    }

    static Object processValue(Object value
, Class type)
    {
        if (value != null && value.getClass() == GStringImpl.class)
        {
            value = value.toString();
        }

        if (type == java.sql.Date.class && value != null)
        {
            value = Utils.changeType(value, java.sql.Date.class);
        }

        return value;
    }

    static void setAttributes(DynamicProperty dp, Map<String, Object> map)
    {
        String displayName = asString(removeFromMap(map, "DISPLAY_NAME"));
        Boolean isHidden = (Boolean) removeFromMap(map, "HIDDEN");

        if (displayName != null) dp.setDisplayName(displayName);
        if (isHidden != null && isHidden) dp.setHidden(true);

        for (Map.Entry<String, Object> attribute : map.entrySet())
        {
            String attributeName = beanInfoConstants.get(attribute.getKey());
            if (attributeName != null)
            {
                dp.setAttribute(attributeName, attribute.getValue());
            }
            else
            {
                log.warning("Not found attribute: " + attribute.getKey() + " in BeanInfoConstants");
            }
        }
    }

    static String asString(Object o)
    {
        return o != null ? o.toString() : null;
    }

    static Object removeFromMap(Map map, Object element)
    {
        if (map.containsKey(element))
        {
            return map.remove(element);
        }
        else
        {
            return null;
        }
    }
}
