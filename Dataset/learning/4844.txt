package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.model.OperationResultPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.inject	.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;

@Singleton
public class FormController extends JsonApiModelController
{
    private final FormGenerator formGenerator;
    private final UserHelper userHelper;
    private final ErrorModelHelper errorModelHelper;
    private final Stage stage;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public FormController(FormGenerator formGenerator,
                          UserHelper userHelper, ErrorModelHelper errorModelHelper,
                          UserInfoProvider userInfoProvider, Stage stage)
    {
        this.formGenerator = formGenerator;
        this.userHelper = userHelper;
        this.errorModelHelper = errorModelHelper;
        this.stage = stage;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String requestSubUrl)
    {
        //todo move to filter
        if (stage == Stage.DEVELOPMENT && userInfoProvider.get() == null)
        {
            userHelper.initGuest();
        }

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        Map<String, Object> operationParams = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.OPERATION_PARAMS));
        Map<String, Object> values = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));

        try
        {
            switch (requestSubUrl)
            {
                case "":
                    ResourceData generateData = formGenerator.generate(entityName, queryName, operationName, operationParams, values);
                    if (OPERATION_RESULT.equals(generateData.getType()) && ((OperationResultPresentation) generateData.getAttributes()).
                        getOperationResult().getStatus() == OperationStatus.ERROR)
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return data(generateData);
                case "apply":
                    ResourceData executeData = formGenerator.execute(entityName, queryName, operationName, operationParams, values);
                    if (FORM_ACTION.equals(executeData.getType()))
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    else if (((OperationResultPresentation) executeData.getAttributes()).
                            getOperationResult().getStatus() == OperationStatus.ERROR)
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return data(executeData);
                default:
                    return null;
            }
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName)
                    .named(operationParams);
            log.log(e.getLogLevel(), "Error in operation: " + url + ", on requestSubUrl = '" + requestSubUrl + "'", e);
            return error(errorModelHelper.getErrorModel(e, Collections.singletonMap(SELF_LINK, url.toString())));
        }
    }

}
