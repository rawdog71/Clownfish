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

import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.UploadUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author sulzbachr
 */
@Scope("request")
@Component
public class UploadTemplateBean implements Serializable {
    private @Getter @Setter String uploadpath;
    private @Getter @Setter List<FileItem> fileitemlist;
    private @Getter @Setter Map<String, Boolean> fileitemmap;
    private PropertyUtil propertyUtil;
    UploadUtil uploadutil;
    private CfTemplateService cftemplateService;
    
    public void init(CfTemplateService cftemplateService, PropertyUtil propertyUtil, CfDatasourceService cfdatasourceService) {
        this.cftemplateService = cftemplateService;
        this.propertyUtil = propertyUtil;
        uploadutil = new UploadUtil(cfdatasourceService);
    }
    
    final transient Logger LOGGER = LoggerFactory.getLogger(UploadTemplateBean.class);
    
    public UploadTemplateBean() {
        fileitemmap = new HashMap<>();
    }
    
    public void clearFileitemMap() {
        fileitemmap.clear();
    }
    
    public void addFileitemMapEntry(String name, boolean upload) {
        fileitemmap.put(name, upload);
    }
    
    public void uploadJson(String ruletemplate, String uploadproperty, String filename) {
        uploadutil.uploadJson(cftemplateService.findByName(ruletemplate).getContent(), propertyUtil.getPropertyValue(uploadproperty), filename);
    }
}
