package com.developmentontheedge.be5.base.model.groovy;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.asString;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.processValue;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.removeFromMap;
import static com.developmentontheedge.be5.base.model.groovy.DynamicPropertyUtils.setAttributes;


public class DynamicPropertySetMetaClass<T extends DynamicPropertySet> extends GDynamicPropertySetMetaClass
{
    public DynamicPropertySetMetaClass(Class<T> theClass)
    {
        super(theClass);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object[] args)
    {
        if ("getAt".equals(methodName))
        {
            return getAt((DynamicPropertySet) object, (String) args[0]);
        }
        else
        {
            return super.invokeMethod(object, methodName, args);
        }
    }

    public Object getAt(DynamicPropertySet dps, String name)
    {
        return dps.getValue(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperty(Object object, String propertyName, Object value)
    {
        DynamicPropertySet dps = ((T) object);
        if (value instanceof Map)
        {
            Map map = (Map) value;
            map.put("name", propertyName);
            this.invokeMethod(object, "leftShift", new Object[]{map});
            return;
        }
        if (value == null)
        {
            DynamicProperty dp = new DynamicProperty(propertyName, String.class);
            dp.setValue(null);
            dps.add(dp);
            return;
        }

        if (dps.getProperty(propertyName) != null)
        {
            dps.setValue(propertyName, processValue(value, dps.getProperty(propertyName).getType()));
            return;
        }

        DynamicProperty dp = new DynamicProperty(propertyName, value.getClass());
        dp.setValue(processValue(value, null));
        dps.add(dp);
    }

    public static DynamicPropertySet leftShift(DynamicPropertySet dps, DynamicProperty property)
    {
        dps.add(property);
        return dps;
    }

    public static DynamicPropertySet leftShift(DynamicPropertySet dps, Map<String, Object> properties)
    {
        Map<String, Object> map = new HashMap<>(properties);

        String name = asString(removeFromMap(map, "name"));
        Objects.requireNonNull(name);
        Class type = (Class) removeFromMap(map, "TYPE");

        boolean isContainValue = map.containsKey("value");
        Object value = processValue(removeFromMap(map, "value"), type);

        DynamicProperty dp = dps.getProperty(name);
        if (dp == null)
        {
            dp = new DynamicProperty(
                    name,
                    type!= null ? type : value != null ? value.getClass() : String.class,
                    value
            );
            dps.add(dp);
        }
        else
        {
            if (type != null) dp.setType(type);
            if (isContainValue) dp.setValue(value);
        }

        setAttributes(dp, map);

        return dps;
    }

    public static DynamicPropertySet plus(DynamicPropertySet dps2, DynamicPropertySet dps)
    {
        DynamicPropertySet clonedDps = new DynamicPropertySetSupport(dps2);
        for (DynamicProperty dp : dps)
        {
            try
            {
                clonedDps.add(DynamicPropertySetSupport.cloneProperty(dp));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return clonedDps;
    }

}
