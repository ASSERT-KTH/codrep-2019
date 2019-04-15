package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.web.Request;
import 
com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;


public abstract class JsonApiModelController extends ApiControllerSupport
{
    @Override
    protected final void generate(Request req, Response res, String subUrl)
    {
        JsonApiModel jsonApiModel = generateJson(req, res, subUrl);
        if (jsonApiModel != null)
        {
            if (jsonApiModel.getMeta() == null)
            {
                jsonApiModel.setMeta(getDefaultMeta(req));
            }
            if (jsonApiModel.getData() != null)
            {
                res.sendAsJson(jsonApiModel);
            }
            else
            {
                int status = Integer.parseInt(jsonApiModel.getErrors()[0].getStatus());
                res.sendAsJson(jsonApiModel, status);
            }
        }
        else
        {
            res.sendAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected abstract JsonApiModel generateJson(Request req, Response res, String subUrl);

    protected JsonApiModel data(ResourceData data)
    {
        return JsonApiModel.data(data, null);
    }

    protected JsonApiModel data(ResourceData data, ResourceData[] included)
    {
        return JsonApiModel.data(data, included, null);
    }

    protected JsonApiModel data(ResourceData data, ErrorModel[] errorModels, ResourceData[] included)
    {
        return JsonApiModel.data(data, errorModels, included, null);
    }

    protected JsonApiModel error(ErrorModel error)
    {
        return JsonApiModel.error(error, null);
    }

    private Object getDefaultMeta(Request request)
    {
        return Collections.singletonMap(TIMESTAMP_PARAM, request.get(TIMESTAMP_PARAM));
    }
}
