package com.developmentontheedge.be5.base.exceptions;

import java.util.EnumMap;
import java.util.List;

import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.ACCESS_DENIED;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.ACCESS_DENIED_TO_OPERATION;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.ACCESS_DENIED_TO_QUERY;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.INTERNAL_ERROR;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION_EXTENDER;
import static com.developmentontheedge.be5.base.exceptions. Be5ErrorCode.INTERNAL_ERROR_IN_QUERY;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.INVALID_STATE;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.NOT_FOUND;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.NOT_INITIALIZED;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.OPERATION_NOT_ASSIGNED_TO_QUERY;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.UNKNOWN_ENTITY;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.UNKNOWN_OPERATION;
import static com.developmentontheedge.be5.base.exceptions.Be5ErrorCode.UNKNOWN_QUERY;


public class ErrorTitles
{
    private static final EnumMap<Be5ErrorCode, String> TITLES = new EnumMap<>(Be5ErrorCode.class);

    static
    {
        TITLES.put(UNKNOWN_ENTITY, "Entity not found: $1");
        TITLES.put(UNKNOWN_QUERY, "Query not found: $1.$2");
        TITLES.put(UNKNOWN_OPERATION, "Operation not found: $1.$2");
        TITLES.put(NOT_FOUND, "Element not found: $1");

        TITLES.put(ACCESS_DENIED, "Access denied. $1");
        TITLES.put(ACCESS_DENIED_TO_QUERY, "Access denied to query: $1.$2");
        TITLES.put(ACCESS_DENIED_TO_OPERATION, "Access denied to operation: $1.$2");
        TITLES.put(OPERATION_NOT_ASSIGNED_TO_QUERY, "Operation '$1.$3' not assigned to query: '$2'");

        TITLES.put(INTERNAL_ERROR_IN_OPERATION, "Internal error occurred during operation: $1.$2");
        TITLES.put(INTERNAL_ERROR_IN_OPERATION_EXTENDER, "Internal error occurred during operation extender: $1");
        TITLES.put(INTERNAL_ERROR_IN_QUERY, "Internal error occurred during query: $1.$2");
        TITLES.put(INTERNAL_ERROR, "Internal error occurred: $1");
        TITLES.put(NOT_INITIALIZED, "Server is not properly initialized: try again later");
        TITLES.put(INVALID_STATE, "Invalid state: $1");

        if (TITLES.size() < Be5ErrorCode.values().length)
        {
            throw new InternalError("Not all error codes have messages!");
        }
    }

    static String formatTitle(Be5ErrorCode code, List<String> parameters)
    {
        return formatTitle(getTitle(code), parameters);
    }

    public static String getTitle(Be5ErrorCode code)
    {
        return TITLES.get(code);
    }

    public static String formatTitle(String title, List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i++)
        {
            title = title.replace("$" + (i + 1), parameters.get(i));
        }

        return title;
    }

}
