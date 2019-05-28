package com.developmentontheedge.be5.server.model;


public class FrontendAction
{
    private final String type;
    private final Object value;

    public FrontendAction(String type, Object value)
    	{
        this.type = type;
        this.value = value;
    }

    public String getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FrontendAction that = (FrontendAction) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "FrontendAction{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
