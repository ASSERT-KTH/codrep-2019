package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.operations.support.DownloadOperationSupport;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DownloadOperationController extends ApiControllerSupport implements Controller
{
    private final OperationExecutor operationExecutor;
    private final UserAwareMeta userAwareMeta;
    private final Validator validator;

    @Inject
    public DownloadOperationController(OperationExecutor operationExecutor, UserAwareMeta userAwareMeta, Validator validator)
    {
        this.operationExecutor = operationExecutor;
        this.userAwareMeta = userAwareMeta;
        this.validator = validator;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        Map<String, Object> operationParams = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.OPERATION_PARAMS));
        Map<String , Object> values = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));

        Operation operation = getOperation(entityName, queryName, operationName, operationParams);
        Object parameters = operationExecutor.generate(operation, values);
        validator.checkAndThrowExceptionIsError(parameters);
        if (operation instanceof DownloadOperationSupport)
        {
            try
            {
                ((DownloadOperationSupport) operation).invokeWithResponse(res, parameters);
            }
            catch (Throwable e)
            {
                throw Be5Exception.internalInOperation(operation.getInfo().getModel(), e);
            }
        }
        else
        {
            throw Be5Exception.internal("operation must extent from DownloadOperationSupport");
        }
    }

    private Operation getOperation(String entityName, String queryName, String operationName, Map<String, Object> operationParams)
    {
        OperationInfo operationInfo = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
        return operationExecutor.create(operationInfo, queryName, operationParams);
    }
}
