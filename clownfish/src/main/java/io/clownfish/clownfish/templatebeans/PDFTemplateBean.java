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

import io.clownfish.clownfish.utils.PDFUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 *
 * @author philip, sulzbachr
 */
@Scope("request")
@Component
public class PDFTemplateBean implements Serializable
{
    @Autowired PDFUtil pdfUtil;

    final transient Logger LOGGER = LoggerFactory.getLogger(PDFTemplateBean.class);

    public PDFTemplateBean()
    {
    }

    public void init(PDFUtil pdfUtil)
    {
        this.pdfUtil = pdfUtil;
    }

    public void initjob(PDFUtil pdfUtil)
    {
        this.pdfUtil = pdfUtil;
    }

    public void generatePDF(String siteName, String param) throws IOException
    {
        pdfUtil.createPDF(siteName, param);
    }
}
