package com.developmentontheedge.be5.server.services.impl;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;


public class MimeMessage2 extends MimeMessage
{
    public static class PrintStream2 extends PrintStream
    {
        public PrintStream2(OutputStream outputStream, boolean autoFlush)
        {
            super(outputStream, autoFlush);
        }

        public ByteArrayOutputStream getOutput()
        {
            return (ByteArrayOutputStream) out;
        }
    }

    public MimeMessage2(Session session)
    {
        super(session);
    }

    public MimeMessage2(Session session, InputStream is)
            throws MessagingException
    {
        super(session, is);
    }

    public Session getSession()
    {
        return session;
    }
}
