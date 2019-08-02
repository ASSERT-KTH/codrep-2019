package com.developmentontheedge.be5.base.exceptions;

import java.util.Arrays;
import java.util.List;


public enum Be5ErrorCode
{
    INTERNAL_ERROR, INTERNAL_ERROR_IN_OPERATION, INTERNAL_ERROR_IN_OPERATION_EXTENDER,
    INTERNAL_ERROR_IN_QUERY, NOT_INITIALIZED,

    ACCESS_DENIED, ACCESS_DENIED_TO_OPERATION, OPERATION_NOT_ASSIGNED_TO_QUERY, ACCESS_DENIED_TO_QUERY,

    UNKNOWN_ENTITY, UNKNOWN_QUERY, UNKNOWN_OPERATION, NOT_FOUND, INVALID_STATE;

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception exception(String... parameters)
    {
        List<String> paramList = Arrays.asList(parameters);
        String msg = ErrorTitles.formatTitle(this, paramList);

        return Be5Exception.create(this, paramList, msg);
    }

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    Be5Exception rethrow(Throwable t, String... parameters)
    {
        List<String> paramList = Arrays.asList(parameters);
        String msg = ErrorTitles.formatTitle(this, paramList);

        return Be5Exception.create(this, paramList, msg, t);
    }

    public boolean isInternal()
    {
        switch (this)
        {
            case INTERNAL_ERROR:
            case INTERNAL_ERROR_IN_OPERATION:
            case INTERNAL_ERROR_IN_OPERATION_EXTENDER:
            case INTERNAL_ERROR_IN_QUERY:
            case NOT_INITIALIZED:
            case INVALID_STATE:
                return true;
            default:
                return false;
        }
    }

    public boolean isAccessDenied()
    {
        switch (this)
        	{
            case ACCESS_DENIED:
            case ACCESS_DENIED_TO_OPERATION:
            case OPERATION_NOT_ASSIGNED_TO_QUERY:
            case ACCESS_DENIED_TO_QUERY:
                return true;
            default:
                return false;
        }
    }

    public boolean isNotFound()
    {
        switch (this)
        {
            case UNKNOWN_ENTITY:
            case UNKNOWN_OPERATION:
            case UNKNOWN_QUERY:
            case NOT_FOUND:
                return true;
            default:
                return false;
        }
    }

    private static final String HTTP_CODE_404 = "404";
    private static final String HTTP_CODE_403 = "403";
    private static final String HTTP_CODE_500 = "500";

    public String getHttpStatus()
    {
        if (isNotFound()) return HTTP_CODE_404;
        if (isAccessDenied()) return HTTP_CODE_403;
        return HTTP_CODE_500;
    }
}
