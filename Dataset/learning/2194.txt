package com.developmentontheedge.be5.base.model;

import com.developmentontheedge.be5.base.model.groovy.DynamicPropertyMetaClass;
import com.developmentontheedge.be5.base.model.groovy.DynamicPropertySetMetaClass;
import com.developmentontheedge.be5.base.model.groovy.GDynamicPropertySetMetaClass;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Map;
import java.util.Objects;


public class GDynamicPropertySetSupport extends DynamicPropertySetSupport
{
    static
    {
        GroovyRegister.registerMetaClass(GDynamicPropertySetMetaClass.class, GDynamicPropertySetSupport.class);
    }

    public GDynamicPropertySetSupport()
    {
        super();
    }

    public GDynamicPropertySetSupport(DynamicPropertySet dps)
    {
        super(dps);
    }

//may be add
//    public DynamicProperty setAt(String name, String value)
//    {
//        DynamicProperty property = new DynamicProperty(name, String.class, value);
//        super.add(property);
//        return property;
//    }

    public DynamicProperty add(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);
        Objects.requireNonNull(builder.getName());
        DynamicProperty property = new DynamicProperty(builder.getName(), builder.getTYPE());
        add(property);
        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
        add(property);

        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName, String displayName,
                               @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        DynamicProperty property = new DynamicProperty(propertyName, builder.getTYPE());
        add(property);
        property.setDisplayName(displayName);

        return DynamicPropertyMetaClass.leftShift(property, builder.getMap());
    }

    public DynamicProperty add(String propertyName)
    {
        return add(propertyName, propertyName, Closure.IDENTITY);
    }

    public DynamicProperty add(String propertyName, String displayName)
    {
        return add(propertyName, displayName, Closure.IDENTITY);
    }

    public DynamicProperty edit(String propertyName,
                                @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);

        return DynamicPropertyMetaClass.leftShift(getProperty(propertyName), builder.getMap());
    }

    public DynamicProperty edit(String propertyName, String displayName,
                                @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DPSAttributes.class) Closure cl)
    {
        DPSAttributes builder = getBuilder(cl);
        builder.setDISPLAY_NAME(displayName);

        return DynamicPropertyMetaClass.leftShift(getProperty(propertyName), builder.getMap());
    }

    private DPSAttributes getBuilder(Closure cl)    {
        DPSAttributes dpsAttributes = new DPSAttributes();
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        cl.setDelegate(dpsAttributes);
        cl.call();
        return dpsAttributes;
    }

    public Object getAt(String name)
    {
        return getValue(name);
    }

    @Deprecated
    public void putAt(String propertyName, Map<String, Object> value)
    {
        value.put("name", propertyName);
        DynamicPropertySetMetaClass.leftShift(this, value);
    }

    @Deprecated
    public DynamicPropertySet leftShift(Map<String, Object> properties)
    {
        return DynamicPropertySetMetaClass.leftShift(this, properties);
    }
}
