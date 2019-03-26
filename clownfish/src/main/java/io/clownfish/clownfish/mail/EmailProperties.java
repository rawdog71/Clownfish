/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.mail;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class EmailProperties {
    private @Getter @Setter String sendto;
    private @Getter @Setter String sendfrom;
    private @Getter @Setter String subject;
    private @Getter @Setter String body;

    public EmailProperties() {
    }

    public EmailProperties(String sendto, String sendfrom, String subject, String body) {
        this.sendto = sendto;
        this.sendfrom = sendfrom;
        this.subject = subject;
        this.body = body;
    }
}
