/*
 * Copyright 2021 raine.
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

import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.jasperreports.JasperReportCompiler;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@ViewScoped
@Component
public class PDFUtil implements Serializable {
    @Autowired CfTemplateService cfTemplateService;
    @Autowired CfTemplateversionService cfTemplateversionService;
    @Autowired CfSitedatasourceService cfSitedatasourceService;
    @Autowired CfDatasourceService cfDatasourceService;
    @Autowired CfSiteService cfSiteService;
    @Autowired PropertyUtil propertyUtil;
    @Autowired TemplateUtil templateUtil;
    
    CfTemplate cfTemplate;
    
    public ByteArrayOutputStream createPDF(String name, String param) throws IOException {
        // Fetch site
        CfSite site = cfSiteService.findByName(name);
        
        HashMap<String, String> params = new HashMap<>();
        // Put request params in HashMap
        if (param != null) {
            String[] arr = param.split("\\$");
            int counter = 0;
            for (String key : arr) {
                if ((counter > 0) && ((counter % 2) != 0)) {
                    params.put(arr[counter-1], arr[counter]);
                }
                counter++;
            }
        }
        
        // Get the current template content
        long currentTemplateVersion;
        try {
            cfTemplate = cfTemplateService.findById(site.getTemplateref().longValue());
            currentTemplateVersion = cfTemplateversionService.findMaxVersion(cfTemplate.getId());
        } catch (NullPointerException ex) {
            currentTemplateVersion = 0;
        }
        String templateContent = templateUtil.getVersion(cfTemplate.getId(), currentTemplateVersion);
        // Search and replace params key/values in template content
        for(String key : params.keySet()) {
            templateContent = templateContent.replaceAll("@" + key + "@", params.get(key));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // Fetch site datasources
        List<CfSitedatasource> sitedatasourcelist = cfSitedatasourceService.findBySiteref(site.getId());
        for (CfSitedatasource source : sitedatasourcelist)
        {
            CfDatasource datasource = cfDatasourceService.findById(source.getCfSitedatasourcePK().getDatasourceref());
            InputStream template = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
            out = JasperReportCompiler.exportToPdf(datasource.getUser(), datasource.getPassword(), datasource.getUrl(), template, datasource.getDriverclass());

            byte[] bytes = out.toByteArray();

            FileOutputStream fileOut;
            StringBuilder stringBuilder = new StringBuilder();

            AtomicInteger count = new AtomicInteger();

            params.forEach((k, v) ->
            {
                count.getAndIncrement();
                if (count.get() < params.size())
                    stringBuilder.append(v).append("-");
                else
                    stringBuilder.append(v);
            });

            fileOut = new FileOutputStream(propertyUtil.getPropertyValue("folder_pdf") + File.separator + name + "-" + stringBuilder + ".pdf");

            fileOut.write(bytes);
            fileOut.close();

            out.write(bytes, 0, bytes.length);
            out.flush();
            out.close();
        }
        return out;
    }
}
