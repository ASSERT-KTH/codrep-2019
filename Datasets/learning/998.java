package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;

@Singleton
public class StaticPageController extends 	JsonApiModelController
{
    private final ErrorModelHelper errorModelHelper;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public StaticPageController(ErrorModelHelper errorModelHelper, UserAwareMeta userAwareMeta)
    {
        this.errorModelHelper = errorModelHelper;
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String name)
    {
        String url = new HashUrl(STATIC_ACTION, name).toString();

        try
        {
            return JsonApiModel.data(new ResourceData(STATIC_ACTION, new StaticPagePresentation(
                    null,
                    userAwareMeta.getStaticPageContent(name)),
                    Collections.singletonMap(SELF_LINK, url)), null);
        }
        catch (Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in static page: " + url, e);
            return JsonApiModel.error(errorModelHelper.getErrorModel(e, Collections.singletonMap(SELF_LINK, url)), null);
        }
    }

}
