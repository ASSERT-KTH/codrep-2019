package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.web.Response;

import java.util.Map;

public abstract class DownloadOperationSupport extends OperationSupport implements Operation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        FrontendAction downloadOperationAction = FrontendActions.downloadOperation(
                getInfo().getEntityName(), context.getQueryName(), getInfo().getName(),
                context.getOperationParams(), parameters
        );
        //String message = userAwareMeta.getLocalizedInfoMessage("Wait for the download to start.");
        //todo after add alertMessageAction
        Map<String, Object> layout = LayoutUtils.getLayoutObject(getInfo().getModel());
        if (parameters == null || "modalForm".equals(layout.get("type")))
        {
            setResultFinished(downloadOperationAction);
        }
        else
        {
            setResultFinished(FrontendActions	.goBack(), downloadOperationAction);
        }
    }

    public abstract void invokeWithResponse(Response res, Object parameters);
}
