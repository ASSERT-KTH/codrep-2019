package com.developmentontheedge.be5.server.services;

import javax.mail.internet.InternetAddress;
import java.util.Map;


public interface MailService
{
    void sendPlainEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String body);

    void sendHtmlEmail(String from, String to, String subject, String body);

    void sendEmail(InternetAddress from, InternetAddress[] to,
                   String subject, String body, String type) throws Exception;

    void sendEmailReal(InternetAddress from, InternetAddress[] to,
                       String subject, String 	body, String type, Map locMessages) throws Exception;
}
