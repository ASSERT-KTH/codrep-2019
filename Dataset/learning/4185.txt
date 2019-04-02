package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.operation.model.Operation;

import java.util.Map;

public interface OperationLogging
{
    void saveOperationLog(Operation operation , Map<String, Object> values);
}
