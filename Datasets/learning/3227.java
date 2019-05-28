package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class InsertOperation extends OperationSupport
{
    protected Object lastInsertID;

    public Object getLastInsertID()
    {
        return lastInsertID;
    }

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps = dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getModel(),
                context.getOperationParams(), presetValues);

        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        lastInsertID = database.getEntity(getInfo().getEntityName()).add((DynamicPropertySet) parameters);

        	setResult(OperationResult.finished());
    }

}
