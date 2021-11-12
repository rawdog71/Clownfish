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

import io.clownfish.clownfish.utils.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
    private transient @Getter @Setter MailUtil mailUtil;
    private transient @Getter @Setter PropertyUtil propertyUtil;

    public EmailTemplateBean() {
    }
    
    public void init(Map<String, String> propertymap, MailUtil mailUtil, PropertyUtil propertyUtil) {
        this.propertymap = propertymap;
        this.mailUtil = new MailUtil(propertyUtil);
        this.propertyUtil = propertyUtil;
    }
    
    public boolean sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        return mailUtil.sendRespondMail(mailto, subject, mailbody);
    }

    public boolean sendRespondMailWithAttachment(String mailto, String subject, String mailbody, String[] attachments) throws Exception {
        return mailUtil.sendRespondMailWithAttachment(mailto, subject, mailbody, attachments);
    }
}