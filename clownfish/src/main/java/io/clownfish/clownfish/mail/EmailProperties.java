/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.mail;

/**
 *
 * @author sulzbachr
 */
public class EmailProperties {
    private String sendto;
    private String sendfrom;
    private String subject;
    private String body;

    public EmailProperties() {
    }

    public EmailProperties(String sendto, String sendfrom, String subject, String body) {
        this.sendto = sendto;
        this.sendfrom = sendfrom;
        this.subject = subject;
        this.body = body;
    }

    public String getSendto() {
        return sendto;
    }

    public void setSendto(String sendto) {
        this.sendto = sendto;
    }

    public String getSendfrom() {
        return sendfrom;
    }

    public void setSendfrom(String sendfrom) {
        this.sendfrom = sendfrom;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
