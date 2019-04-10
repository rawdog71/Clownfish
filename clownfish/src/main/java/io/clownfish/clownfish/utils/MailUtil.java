/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.utils;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class MailUtil {
    private @Getter @Setter String mailsmtphost;
    private @Getter @Setter String mailtransportprotocol;
    private @Getter @Setter String mailuser;
    private @Getter @Setter String mailpassword;
    private @Getter @Setter String sendfrom;
    private final Properties props;
    private final String encodingOptions = "text/html; charset=UTF-8";

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

    public void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        Session session = Session.getInstance(props, null);

        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setHeader("Content-Type", encodingOptions);
        message.setFrom(new InternetAddress(sendfrom));
        if (mailto.indexOf(",") > 0) {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailto));
        } else {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailto));
        }
        message.setSubject(subject);
        message.setContent(mailbody, encodingOptions);

        if (mailto.compareToIgnoreCase("noreply@clownfish.io") != 0) {
        // Send the message
            Transport.send( message );
        }
    }
}
