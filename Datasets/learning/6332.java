package com.developmentontheedge.be5.server.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class Jaxb
{
    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> klass, String url) throws AssertionError
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(klass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(new URL(url));
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException();
        }
        catch (JAXBException e)
        {
            throw new RuntimeException();
        }
    }

    public static <T> String toXml(Class<T> klass, T object)
    {
        StringWriter out = new StringWriter();

        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(klass);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(object, out);
        }
        catch (JAXBException e)        {
            throw new RuntimeException(e);
        }

        return out.toString();
    }

}
