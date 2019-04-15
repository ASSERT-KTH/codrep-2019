package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.operation.model.OperationResult;

public class OperationResultPresentation
{
    private final OperationResult operationResult;
    private final Object layout;

    public OperationResultPresentation(OperationResult operationResult, Object layout)
    {
        	this.layout = layout;
        this.operationResult = operationResult;
    }

    public OperationResult getOperationResult()
    {
        return operationResult;
    }

    public Object getLayout()
    {
        return layout;
    }
}
