package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationContext;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.services.OperationsFactory;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.operation.support.BaseOperationSupport;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.helpers	.DpsHelper;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;


public abstract class OperationSupport extends BaseOperationSupport implements Operation
{
    @Inject
    public Meta meta;
    @Inject
    public UserAwareMeta userAwareMeta;
    @Inject
    public DbService db;
    @Inject
    public DatabaseModel database;
    @Inject
    public DpsHelper dpsHelper;
    @Inject
    public Validator validator;
    @Inject
    public OperationsFactory operations;
    @Inject
    public QueriesService queries;

    @Inject
    protected Session session;
    @Inject
    protected Request request;

    protected UserInfo userInfo;

    @Override
    public final void initialize(OperationInfo info, OperationContext context, OperationResult operationResult)
    {
        super.initialize(info, context, operationResult);

        this.userInfo = (UserInfo) session.get(SessionConstants.USER_INFO);
    }

    public Query getQuery()
    {
        return meta.getQuery(getInfo().getEntityName(), context.getQueryName());
    }

    public void setResultFinished()
    {
        setResult(OperationResult.finished());
    }

    public void setResultFinished(String message)
    {
        setResult(OperationResult.finished(message));
    }

    public void setResultFinished(String message, FrontendAction... frontendActions)
    {
        setResult(OperationResult.finished(message, frontendActions));
    }

    public void setResultFinished(FrontendAction... frontendActions)
    {
        setResult(OperationResult.finished(null, frontendActions));
    }
}
