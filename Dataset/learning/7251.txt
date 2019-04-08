package com.developmentontheedge.be5.base.model.groovy;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import groovy.lang.MissingPropertyException;


public class GDynamicPropertySetMetaClass<T extends DynamicPropertySet> extends ExtensionMethodsMetaClass
{
    public GDynamicPropertySetMetaClass(Class<T> theClass)
    {
        super(theClass);
    }

    public Object getProperty(Object object, String property)
    {
        if (PropertyAccessHelper.isValueAccess(property))
        {
            return ((T) object).getValue(property.substring(1));
        }
        try
        {
            return super.getProperty(object, property);
        }
        catch (MissingPropertyException e)        {
            if (PropertyAccessHelper.isPropertyAccess(property))
            {
                DynamicProperty prop = ((T) object).getProperty(property.substring(1));
                if (prop != null)
                {
                    return prop;
                }
            }
            DynamicProperty prop = ((T) object).getProperty(property);
            if (prop != null)
            {
                return prop;
            }
            throw e;
        }
    }

}
