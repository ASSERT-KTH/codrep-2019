package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.impl.ControllerSupport;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.be5.web.impl.ResponseImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class ApiControllerSupport extends ControllerSupport
{
    protected static final Logger log = Logger.getLogger(ApiControllerSupport.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        respond(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        respond(request, response);
    }

    private void respond(HttpServletRequest request, HttpServletResponse response)
    {
        ServletUtils.addHeaders(request, response);
        Request req = new RequestImpl(request);
        Response res = new ResponseImpl(response);
        try
        {
            generate(req, res);
        }
        catch (Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in controller", e);
            res.sendAsJson(e.getMessage(), Integer.parseInt(e.getHttpStatusCode()));
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in controller", e);
            res.sendAsJson("Error in controller", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public final void generate(Request req, Response res)
    {
        try
        {
            generate(req, res, getApiSubUrl(req));
        }
        catch (IllegalArgumentException e)
        {
            res.sendAsJson(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected abstract void generate(Request req, Response res, String subUrl);

    private String getApiSubUrl(Request req)
    {
        String requestUri = req.getRequestUri();

        String apiWithContext = req.getContextPath() + "/api/";

        int subIndex = requestUri.indexOf('/', apiWithContext.length());

        if (subIndex != -1)
        {
            return requestUri.substring(subIndex + 1, requestUri.length());
        }
        else
        {
            return 
"";
        }
    }
}
