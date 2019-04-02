package com.developmentontheedge.be5.base.model.groovy;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import groovy.lang.GroovyObjectSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.beanInfoConstants;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.processValue;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.removeFromMap;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.setAttributes;

/**
 * Created by ruslan on 26.11.15.
 */
public class DynamicPropertyMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
{
    private static final Logger log = Logger.getLogger(DynamicPropertySetMetaClass.class.getName());

    public DynamicPropertyMetaClass(Class<T> theClass)
    {
        super(theClass);
    }

    public static final class AttributeAccessor extends GroovyObjectSupport
    {
        private final DynamicProperty dp;

        private AttributeAccessor(DynamicProperty dp)
        
{
            this.dp = dp;
        }

        @Override
        public Object getProperty(String name)
        {
            String attributeName = beanInfoConstants.get(name);
            return dp.getAttribute(attributeName != null ? attributeName : name);
        }

        @Override
        public void setProperty(String name, Object value)
        {
            String attributeName = beanInfoConstants.get(name);
            dp.setAttribute(attributeName != null ? attributeName : name, value);
        }
    }

    @Override
    public Object getProperty(Object object, String property)
    {
        DynamicProperty dp = (DynamicProperty) object;
        if ("attr".equals(property))
        {
            return new AttributeAccessor(dp);
        }
        return super.getProperty(object, property);
    }

    public static DynamicProperty leftShift(DynamicProperty dp, Map<String, Object> properties)
    {
        Map<String, Object> map = new HashMap<>(properties);

        removeFromMap(map, "name");

        Class type = (Class) removeFromMap(map, "TYPE");
        if (type != null) dp.setType(type);

        if (map.containsKey("value"))
        {
            Object value = processValue(removeFromMap(map, "value"), type);
            dp.setValue(value);
        }

        setAttributes(dp, map);

        return dp;
    }
}
