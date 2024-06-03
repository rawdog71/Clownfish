/*
 * Copyright 2019 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish.utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
@Scope("singleton")
public class MailUtil implements Serializable {
    private @Getter @Setter PropertyUtil propertyUtil;
    private @Getter @Setter String mailsslenabled;
    private @Getter @Setter String mailsmtpport;
    private @Getter @Setter String mailsmtphost;
    private @Getter @Setter String mailtransportprotocol;
    private @Getter @Setter String mailuser;
    private @Getter @Setter String mailpassword;
    private @Getter @Setter String sendfrom;
    private Properties props = null;
    private final String encodingOptions = "text/html; charset=UTF-8";
    
    final transient Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    public MailUtil() { 
        props = System.getProperties(); 
    }

    public MailUtil(PropertyUtil propertyUtil) {
        if (null != propertyUtil) {
            this.propertyUtil = propertyUtil;

            this.mailsslenabled = propertyUtil.getPropertyValue("mail_ssl_enabled");
            this.mailsmtphost = propertyUtil.getPropertyValue("mail_smtp_host");
            this.mailsmtpport = propertyUtil.getPropertyValue("mail_smtp_port");
            this.mailtransportprotocol = propertyUtil.getPropertyValue("mail_transport_protocol");
            this.mailuser = propertyUtil.getPropertyValue("mail_user");
            this.mailpassword = propertyUtil.getPropertyValue("mail_password");
            this.sendfrom = propertyUtil.getPropertyValue("mail_sendfrom");

            props = System.getProperties();
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
            props.put("mail.smtp.host", propertyUtil.getPropertyValue("mail_smtp_host"));
            props.put("mail.transport.protocol", propertyUtil.getPropertyValue("mail_transport_protocol"));
            props.put("mail.user", propertyUtil.getPropertyValue("mail_user"));
            props.put("mail.password", propertyUtil.getPropertyValue("mail_password"));
            props.put("mail.smtp.socketFactory.port", propertyUtil.getPropertyValue("mail_smtp_port")); //SSL Port
            props.put("mail.smtp.auth", propertyUtil.getPropertyValue("mail_ssl_enabled")); //Enabling SMTP Authentication
            props.put("mail.smtp.port", propertyUtil.getPropertyValue("mail_smtp_port")); //SMTP Port
        }
    }

    public boolean sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        Session session;
        if (0 == propertyUtil.getPropertyValue("mail_ssl_enabled").compareToIgnoreCase("true")) {
            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(propertyUtil.getPropertyValue("mail_sendfrom"), propertyUtil.getPropertyValue("mail_password"));
                }
            };
            session = Session.getDefaultInstance(props, auth);
        } else {
            session = Session.getInstance(props, null);
        }
        
        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setHeader("Content-Type", encodingOptions);
        message.setFrom(new InternetAddress(sendfrom));
        if (mailto.indexOf(',') > 0) {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailto));
        } else {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailto));
        }
        message.setSubject(subject);
        message.setContent(mailbody, encodingOptions);

        if (mailto.compareToIgnoreCase("noreply@clownfish.io") != 0) {
            // Send the message
            try {
                Transport.send( message );
                return true;
            } catch (MessagingException ex) {
                LOGGER.error(ex.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean sendRespondMailWithAttachment(String mailto, String subject, String mailbody, String[] attachments) throws Exception {
        Session session = Session.getInstance(props, null);

        // Define message
        MimeMessage message = new MimeMessage(session);
        Multipart multiPart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setText(mailbody);
        multiPart.addBodyPart(messageBodyPart);

        ArrayList<MimeBodyPart> attachmentBodies = new ArrayList<>();
        int count = 0;

        for (String fileName : attachments)
        {
            File file = new File(propertyUtil.getPropertyValue("folder_attachments") + File.separator + fileName);
            attachmentBodies.add(new MimeBodyPart());
            attachmentBodies.get(count).attachFile(file);
            multiPart.addBodyPart(attachmentBodies.get(count));
            count++;
        }

        message.setHeader("Content-Type", encodingOptions);
        message.setFrom(new InternetAddress(sendfrom));
        if (mailto.indexOf(',') > 0)
        {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailto));
        }
        else
        {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailto));
        }
        message.setSubject(subject);
        message.setContent(multiPart, encodingOptions);

        if (mailto.compareToIgnoreCase("noreply@clownfish.io") != 0)
        {
            // Send the message
            try
            {
                Transport.send( message );
                return true;
            }
            catch (MessagingException ex)
            {
                LOGGER.error(ex.getMessage());
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}
