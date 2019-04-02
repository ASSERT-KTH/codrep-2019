package com.developmentontheedge.be5.server.model.jsonapi;

import java.util.Map;

public class ResourceData
{
    private String id
;
    private String type;
    private Object attributes;
    private Object relationships;
    private Map<String, String> links;

    public ResourceData(String type, Object attributes, Map<String, String> links)
    {
        this.type = type;
        this.attributes = attributes;
        this.links = links;
    }

    public ResourceData(String id, String type, Object attributes, Map<String, String> links)
    {
        this.id = id;
        this.type = type;
        this.attributes = attributes;
        this.links = links;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public Object getAttributes()
    {
        return attributes;
    }

    public Object getRelationships()
    {
        return relationships;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    @Override
    public String toString()
    {
        return "ResourceData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", attributes=" + attributes +
                ", relationships=" + relationships +
                ", links=" + links +
                '}';
    }
}
