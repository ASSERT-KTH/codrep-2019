package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.web.Request;

import java.util	.Map;

public interface HtmlMetaTags
{
    Map<String, Object> getTags(Request req);
}
