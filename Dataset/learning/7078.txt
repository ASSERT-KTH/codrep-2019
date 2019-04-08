package com.developmentontheedge.be5.base.exceptions;

import com.developmentontheedge.be5.base.util.HtmlUtils;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SourceFileOperationExtender;

import java.util.List;
import java.util.logging.Level;


public class Be5Exception extends RuntimeException
{
    private static final long serialVersionUID = 9189259622768482031L;

    private final Be5ErrorCode code;
    private final List<String> parameters;

    private Be5Exception(Be5ErrorCode code, List<String> parameters, String message, Throwable cause)
    {
        super(message, cause);
        this.code = code;
        this.parameters = parameters;
    }

    private Be5Exception(Be5ErrorCode code, List<String> parameters, String message)
    {
        this(code, parameters, message, null);
    }

//    private Be5Exception(Be5ErrorCode code, Throwable t, Object... parameters)
//    {
//        super(ErrorTitles.formatTitle(code, parameters), t);
//
//        title = ErrorTitles.formatTitle(code, parameters);
//
//        this.code = code;
//    }

    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    static Be5Exception create(Be5ErrorCode code, List<String> parameters, String title)
    {
        return new Be5Exception(code, parameters, title);
    }

    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    static Be5Exception create(Be5ErrorCode code, List<String> parameters, String message, Throwable t)
    {
        return new Be5Exception(code, parameters, message, t);
    }

    public static Be5Exception accessDenied()
    {
        return Be5ErrorCode.ACCESS_DENIED.exception(" ");
    }

    public static Be5Exception accessDenied(String info)
    {
        return Be5ErrorCode.ACCESS_DENIED.exception(info);
    }

    public static Be5Exception accessDeniedToOperation(String entityName, String operationName)
    {
        return Be5ErrorCode.ACCESS_DENIED_TO_OPERATION.exception(entityName, operationName);
    }

    public static Be5Exception accessDeniedToQuery(String entityName, String queryName)
    {
        return Be5ErrorCode.ACCESS_DENIED_TO_QUERY.exception(entityName, queryName);
    }

    public static Be5Exception internal(String title)
    {
        return Be5ErrorCode.INTERNAL_ERROR.exception(title);
    }

    public static Be5Exception internal(Throwable cause)
    {
        return internal("", cause);
    }

    public static Be5Exception internal(String message, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(cause, message);
    }

    public static Be5Exception internalInQuery(Query q, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_QUERY.rethrow(cause, q.getEntity().getName(), q.getName());
    }

    public static Be5Exception internalInOperation(Operation o, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(cause, o.getEntity().getName(), o.getName());
    }

    public static Be5Exception operationNotAssignedToQuery(String entityName, String queryName, String name)
    {
        return Be5ErrorCode.OPERATION_NOT_ASSIGNED_TO_QUERY.exception(entityName, queryName, name);
    }

    public static Be5Exception internalInOperationExtender(OperationExtender operationExtender, Throwable cause)
    {
        String name;
        if (operationExtender instanceof SourceFileOperationExtender)
        {
            name = ((SourceFileOperationExtender) operationExtender).getFileName();
        }
        else
        {
            name = operationExtender.getClassName();
        }
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION_EXTENDER.rethrow(cause, name);
    }

    public static Be5Exception unknownEntity(String entityName)
    {
        return Be5ErrorCode.UNKNOWN_ENTITY.exception(entityName);
    }

    public static Be5Exception unknownQuery(String entityName, String queryName)
    {
        return Be5ErrorCode.UNKNOWN_QUERY.exception(entityName, queryName);
    }

    public static Be5Exception unknownOperation(String entityName, String operationName)
    {
        return Be5ErrorCode.UNKNOWN_OPERATION.exception( entityName, operationName);
    }

    public static Be5Exception notFound(String element)
    {
        return Be5ErrorCode.NOT_FOUND.exception(element);
    }

    public static Be5Exception invalidState(String title)
    {
        return Be5ErrorCode.INVALID_STATE.exception(title);
    }

    public Be5ErrorCode getCode()
    {
        return code;
    }

//    public String getMessage()
//    {
//        return generatedMessage;
//    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public static String getMessage(Throwable err)
    {
        Throwable e = err;
        StringBuilder out = new StringBuilder(getThrowableMessage(e));

        while (e instanceof Be5Exception && e.getCause() != null)
        {
            e = e.getCause();
            out.append(getThrowableMessage(e));
        }
        if (e.getClass() == NullPointerException.class)
        {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 2); i++)
            {
                out.append(getFullStackTraceLine(stackTrace[i])).append("\n");
            }
        }

        return HtmlUtils.escapeHTML(out.toString());
    }

    public static String getFullStackTraceLine(StackTraceElement e)
    {
        return e.getClassName() + "." + e.getMethodName()
                + "(" + e.getFileName() + ":" + e.getLineNumber() + ")";
    }

    private static String getThrowableMessage(Throwable e)
    {
        if (e instanceof Be5Exception)
        {
            return e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
        }
        else
        {
            return e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n";
        }
    }

    //todo refactor to int and use HttpServletResponse.* constants
    public String getHttpStatusCode()
    {
        return code.getHttpStatus();
    }

    public Level getLogLevel()
    {
        if (getCode().isInternal()) return Level.SEVERE;
        else return Level.INFO;
    }
}
