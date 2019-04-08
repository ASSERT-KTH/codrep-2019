package com.developmentontheedge.be5.base.model.groovy;

import com.google.common.collect.ObjectArrays;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;


/**
 * Created by ruslan on 14.09.16.
 */
public class ExtensionMethodsMetaClass extends DelegatingMetaClass
{
    protected ExtensionMethodsMetaClass(Class classForExtension)
    {
        super (classForExtension);
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object[] args)
    {
        try
        {
            return InvokerHelper.invokeMethod(getClass(), methodName, ObjectArrays.concat(object, args));
        }
        catch (MissingMethodException e)
        { /*missing method in meta-class*/ }

        return super.invokeMethod(object, methodName, args);
    }

}