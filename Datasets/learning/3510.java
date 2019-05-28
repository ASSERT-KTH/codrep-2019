package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.FrontendConstants.RELOAD_CONTROL_NAME;

public class EventManager implements MethodInterceptor
{
    public static final Logger log = Logger.getLogger(EventManager.class.getName());
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_OPERATION = "operation";
    public static final String ACTION_QUERY_BUILDER = "queryBuilder";
    public static final String ACTION_LOGGING = "logging";
    public static final String ACTION_PRINT = "print";
    public 
static final String ACTION_SERVLET = "servlet";
    public static final String ACTION_PROCESS = "process";
    public static final String ACTION_OTHER = "other";

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Object[] arguments = invocation.getArguments();
        String className = invocation.getMethod().getDeclaringClass().getSimpleName();
        if (className.equals("DocumentGeneratorImpl"))
        {
            return logQuery(invocation, startTime, arguments);
        }
        if (className.equals("FormGeneratorImpl"))
        {
            return logOperation(invocation, startTime, arguments);
        }

        return invocation.proceed();
    }

    private Object logQuery(MethodInvocation invocation, long startTime, Object[] arguments) throws Throwable
    {
        Query query = (Query) arguments[0];
        Map<String, Object> parameters = (Map<String, Object>) arguments[1];
        try
        {
            Object proceed = invocation.proceed();
            queryCompleted(query, parameters, startTime, System.currentTimeMillis());
            return proceed;
        } catch (Throwable e)
        {
            queryError(query, parameters, startTime, System.currentTimeMillis(), e.getMessage());
            throw e;
        }
    }

    private Object logOperation(MethodInvocation invocation, long startTime, Object[] arguments) throws Throwable
    {
        Operation operation = (Operation) arguments[0];
        Map<String, Object> values = (Map<String, Object>) arguments[1];

        Object proceed = invocation.proceed();
        if (operation.getStatus() == OperationStatus.ERROR)
        {
            operationError(operation, values, startTime, System.currentTimeMillis(), operation.getResult().getMessage());
        }
        else if (!values.containsKey(RELOAD_CONTROL_NAME))
        {
            operationCompleted(operation, values, startTime, System.currentTimeMillis());
        }
        return proceed;
    }

    public void operationCompleted(Operation operation, Map<String, Object> values,
                                   long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.operationCompleted(operation, values, startTime, endTime);
        }
    }

    public void operationError(Operation operation, Map<String, Object> values,
                               long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.operationError(operation, values, startTime, endTime, exception);
        }
    }

    public void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryCompleted(query, parameters, startTime, endTime);
        }
    }

    public void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryError(query, parameters, startTime, endTime, exception);
        }
    }

//    public void servletStarted( ServletInfo si ) //, ...
//
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//
//                listener.servletStarted( si );
//
//        }
//    }
//
//    public void servletCompleted( ServletInfo si )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//
//                listener.servletCompleted( si );
//
//        }
//    }
//
//    public void servletDenied( ServletInfo si, String reason )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.servletDenied( si, reason );
//        }
//    }

    ///////////////////////////////////////////////////////////////////
    // methods for long processes and daemons
    //

//    static public void processStateChanged(ProcessInfo pi)
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.processStateChanged(pi);
//        }
//    }

    private final List<Be5EventLogger> listeners = new ArrayList<>();

    public void addListener(Be5EventLogger listener)
    {
        listeners.add(listener);
    }

    public void removeListener(Be5EventLogger listener)
    {
        listeners.remove(listener);
    }

}
