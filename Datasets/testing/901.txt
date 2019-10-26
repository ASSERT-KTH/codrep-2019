package com.developmentontheedge.be5.server.model;


import java.util.Arrays;
import java.util.Base64;

public class Base64File
{
    private String name;
    private byte[] data;
    private String mimeTypes;

    public Base64File(String name, byte[] data, String mimeTypes)
    {
        this.name = name;
        this.data = data;
        this.mimeTypes = mimeTypes;
    }

    public String getName()
    {
        return name;
    }

    public byte[] getData()
    {
        return data;
    }

    public String getMimeTypes()
    {
        return mimeTypes;
    }

    @Override
    public String toString()
    {
        return "{\"type\":\"Base64File\",\"name\":\"" + name +
                "\", \"data\":\"data:" + mimeTypes + ";base64," + Base64.getEncoder().encodeToString(data) + "\"}";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Base64File that = (Base64File) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!Arrays.equals(data, that.data)) return false;
        return mimeTypes != null ? mimeTypes.equals(that.mimeTypes) : that.mimeTypes == null;
    }

    @Override
    public int hashCode()
    {
        int result = name != 
null ? name.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + (mimeTypes != null ? mimeTypes.hashCode() : 0);
        return result;
    }
}
