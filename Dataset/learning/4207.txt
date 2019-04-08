package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;import java.util.HashMap;
import java.util.Map;


public class HtmlMetaTagsImpl implements HtmlMetaTags
{
    private final UserAwareMeta userAwareMeta;
    private final Meta meta;

    @Inject
    public HtmlMetaTagsImpl(UserAwareMeta userAwareMeta, Meta meta)
    {
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;
    }

    @Override
    public Map<String, Object> getTags(Request req)
    {
        String title = userAwareMeta.getColumnTitle("index", "page", "title");
        String description = userAwareMeta.getColumnTitle("index", "page", "description");

        Map<String, Object> values = new HashMap<>();

        values.put("lang", meta.getLocale(null));
        values.put("title", title);
        values.put("description", description);

        return values;
    }
}
