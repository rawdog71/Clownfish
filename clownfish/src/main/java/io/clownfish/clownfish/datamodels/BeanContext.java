/*
 * Copyright 2026 SulzbachR.
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
package io.clownfish.clownfish.datamodels;

import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.templatebeans.ContentTemplateBean;
import io.clownfish.clownfish.templatebeans.DatabaseTemplateBean;
import io.clownfish.clownfish.templatebeans.DownloadTemplateBean;
import io.clownfish.clownfish.templatebeans.EmailTemplateBean;
import io.clownfish.clownfish.templatebeans.ExternalClassProvider;
import io.clownfish.clownfish.templatebeans.ImportTemplateBean;
import io.clownfish.clownfish.templatebeans.JSONatorBean;
import io.clownfish.clownfish.templatebeans.NetworkTemplateBean;
import io.clownfish.clownfish.templatebeans.PDFTemplateBean;
import io.clownfish.clownfish.templatebeans.SAPTemplateBean;
import io.clownfish.clownfish.templatebeans.UploadTemplateBean;
import io.clownfish.clownfish.templatebeans.WebServiceTemplateBean;
import io.clownfish.clownfish.templatebeans.WebSocketTemplateBean;
import lombok.Data;

/**
 *
 * @author SulzbachR
 */
@Data
public class BeanContext {
    private DatabaseTemplateBean databasebean;
    private ContentTemplateBean contentbean;
    private EmailTemplateBean emailbean;
    private SAPTemplateBean sapbean;
    private NetworkTemplateBean networkbean;
    private WebServiceTemplateBean webservicebean;
    private ImportTemplateBean importbean;
    private PDFTemplateBean pdfbean;
    private ExternalClassProvider externalclassproviderbean;
    private SiteTreeBean sitetree;
    private JSONatorBean jsonatorbean;
    private UploadTemplateBean uploadbean;
    private WebSocketTemplateBean websocketbean;
    private DownloadTemplateBean downloadbean;
}
