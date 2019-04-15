package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletResponse;


public abstract class JsonApiController extends ApiControllerSupport
{
    @Override
    protected final void generate(Request req, Response res, String subUrl)
    {
        Object object = generate
(req, subUrl);
        if (object != null)
        {
            res.sendAsJson(object);
        }
        else
        {
            res.sendAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected abstract Object generate(Request req, String subUrl);
}
