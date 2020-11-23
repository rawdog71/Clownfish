/*
 * Copyright 2020 SulzbachR.
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

import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class HibernateInit {
    private @Getter @Setter ServiceStatus serviceStatus;
    private @Getter @Setter CfClassService cfclassservice;
    private @Getter @Setter CfAttributService cfattributservice;
    private @Getter @Setter CfClasscontentService cfclasscontentService;
    private @Getter @Setter CfAttributcontentService cfattributcontentService;
    private @Getter @Setter CfListcontentService cflistcontentService;
    private @Getter @Setter CfClasscontentKeywordService cfclasscontentkeywordService;
    private @Getter @Setter CfKeywordService cfkeywordService;
    private @Getter @Setter String datasourceURL;

    public HibernateInit(ServiceStatus serviceStatus, CfClassService cfclassservice, CfAttributService cfattributservice, CfClasscontentService cfclasscontentService, CfAttributcontentService cfattributcontentService, CfListcontentService cflistcontentService, CfClasscontentKeywordService cfclasscontentkeywordService, CfKeywordService cfkeywordService, String datasourceURL) {
        this.serviceStatus = serviceStatus;
        this.cfclassservice = cfclassservice;
        this.cfattributservice = cfattributservice;
        this.cfclasscontentService = cfclasscontentService;
        this.cfattributcontentService = cfattributcontentService;
        this.cflistcontentService = cflistcontentService;
        this.cfclasscontentkeywordService = cfclasscontentkeywordService;
        this.cfkeywordService = cfkeywordService;
        this.datasourceURL = datasourceURL;
    }
}
