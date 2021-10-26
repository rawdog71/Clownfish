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
package io.clownfish.clownfish.templatebeans;

import io.clownfish.clownfish.utils.MailUtil;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class EmailTemplateBean implements Serializable {
    private transient @Getter @Setter Map<String, String> propertymap = null;

    public EmailTemplateBean() {
    }
    
    public void init(Map<String, String> propertymap) {
        this.propertymap = propertymap;
    }
    
    public boolean sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        MailUtil mailutil = new MailUtil(propertymap.get("mail_smtp_host"), propertymap.get("mail_transport_protocol"), propertymap.get("mail_user"), propertymap.get("mail_password"), propertymap.get("mail_sendfrom"));
        return mailutil.sendRespondMail(mailto, subject, mailbody);
    }

    public boolean sendRespondMailWithAttachment(String mailto, String subject, String mailbody, String[] attachments) throws Exception {
        MailUtil mailutil = new MailUtil(propertymap.get("mail_smtp_host"), propertymap.get("mail_transport_protocol"), propertymap.get("mail_user"), propertymap.get("mail_password"), propertymap.get("mail_sendfrom"));
        return mailutil.sendRespondMailWithAttachment(mailto, subject, mailbody, attachments);
    }
}
