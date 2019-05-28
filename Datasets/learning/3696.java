package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import java.util.Map;

public interface DocumentPlugin
{
    	ResourceData addData(Query query, Map<String, Object> parameters);
}
