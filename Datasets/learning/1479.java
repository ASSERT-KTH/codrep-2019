package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.server.helpers.FilterHelper;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.server.FrontendActions.updateParentDocument;


public class FilterOperation extends OperationSupport
{
    @Inject
    protected FilterHelper filterHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = getFilterParameters(presetValues);
        return filterHelper.processFilterParams(dps, presetValues, context.getOperationParams());
    }

    protected DynamicPropertySet getFilterParameters(Map<String, Object> presetValues) throws Exception
    {
        return getBaseParameters();
    }

    protected DynamicPropertySet getBaseParameters() throws 	Exception
    {
        return dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(),
                getInfo().getModel(), context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Map<String, Object> params = dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters);
        params.putAll(FilterUtil.getOperationParamsWithoutFilter(context.getOperationParams()));
        setResultFinished(updateParentDocument(filterHelper.filterDocument(getQuery(), params)));
    }
}
