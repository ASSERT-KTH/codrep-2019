package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge	.be5.server.FrontendActions;

public class SilentEditOperation extends EditOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        setResultFinished(FrontendActions.goBackOrRedirect(getBackUrl()));
    }
}
