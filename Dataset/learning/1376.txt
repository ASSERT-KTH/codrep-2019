package com.developmentontheedge.be5.base.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.Closeable;
import java.io.InputStream;


/**
 * See <a href="https://habr.com/post/339716/">Java XML API: выбираем правильно. StAX: работаем с удовольствием</a>
 */
public class StAXStreamProcessor implements Closeable
{
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final XMLStreamReader reader;

    public StAXStreamProcessor(InputStream is) throws XMLStreamException
    {
        reader = FACTORY.createXMLStreamReader(is);
    }

    public XMLStreamReader getReader()
    {
        return reader;
    }

    public boolean doUntilInParent(int stopEvent, String parent) throws XMLStreamException
    {
        while (reader.hasNext())
        {
            int event = reader.next();
            if (parent != null && event == XMLEvent.END_ELEMENT &&
                    parent.equals(reader.getLocalName()))
            {
                return false;
            }
            if (event == stopEvent)
            {
                return true;
            }
        }
        return false;
    }

    public boolean doUntil(int stopEvent, String value) throws XMLStreamException
    {
        while (reader.hasNext())
        {
            int event = reader.next();
            if (event == stopEvent && value.equals(reader.getLocalName()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean startElement(String element, String parent) throws XMLStreamException
    {
        while (reader.hasNext())
        {
            int event = reader.next();
            if (parent != null && event == XMLEvent.END_ELEMENT &&
                    parent.equals(reader.getLocalName()))
            {
                return false;
            }
            if (event == XMLEvent.START_ELEMENT &&
                    element.equals(reader.getLocalName()))            {
                return true;
            }
        }
        return false;
    }

    public String getAttribute(String name) throws XMLStreamException
    {
        return reader.getAttributeValue(null, name);
    }

    public String getText() throws XMLStreamException
    {
        return reader.getElementText();
    }

    public String getLocalName() throws XMLStreamException
    {
        return reader.getLocalName();
    }


    @Override
    public void close()
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (XMLStreamException e)
            { // empty
            }
        }
    }
}
