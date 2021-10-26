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
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.faces.view.ViewScoped;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Accessors(chain = true)
@Component
@ViewScoped
public class MailUtil implements Serializable {
    private @Getter @Setter String mailsmtphost;
    private @Getter @Setter String mailtransportprotocol;
    private @Getter @Setter String mailuser;
    private @Getter @Setter String mailpassword;
    private @Getter @Setter String sendfrom;
    private final Properties props;
    private final String encodingOptions = "text/html; charset=UTF-8";
    @Autowired PropertyUtil propertyUtil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    public MailUtil() { props = System.getProperties(); }

    public MailUtil(String mailsmtphost, String mailtransportprotocol, String mailuser, String mailpassword, String sendfrom) {
        props = System.getProperties();
        
        this.mailsmtphost = mailsmtphost;
        this.mailtransportprotocol = mailtransportprotocol;
        this.mailuser = mailuser;
        this.mailpassword = mailpassword;
        this.sendfrom = sendfrom;
        
        props.put("mail.smtp.host", mailsmtphost);
        props.put("mail.transport.protocol", mailtransportprotocol);
        props.put("mail.user", mailuser);
        props.put("mail.password", mailpassword);
    }

    public boolean sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        Session session = Session.getInstance(props, null);

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
            } catch (Exception ex) {
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
            catch (Exception ex)
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
