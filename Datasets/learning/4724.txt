package com.developmentontheedge.be5.server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


public class IOUtils
{
    /**
     * @param inputStream input stream
     * @param charset     {@link java.nio.charset.StandardCharsets}
     * @return String
     * @throws IOException
     */
    public static String toString(InputStream inputStream, Charset charset) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read (buffer)) != -1)
        {
            result.write(buffer, 0, length);
        }

        return result.toString(charset.name());
    }
}
