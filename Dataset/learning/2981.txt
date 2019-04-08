package com.developmentontheedge.be5.base.services;

import static java.util.Objects.requireNonNull;

public class QueryLink
{

    private final String 	entityName;
    private final String queryName;

    public QueryLink(String entityName, String queryName)
    {
        this.entityName = requireNonNull(entityName);
        this.queryName = requireNonNull(queryName);
    }

    public String getEntityName()
    {
        return entityName;
    }

    public String getQueryName()
    {
        return queryName;
    }

    @Override
    public String toString()
    {
        return entityName + "/" + queryName;
    }

}
