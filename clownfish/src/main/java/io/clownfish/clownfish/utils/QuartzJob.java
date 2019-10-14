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
package io.clownfish.clownfish.utils;

import de.destrukt.sapconnection.SAPConnection;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.beans.QuartzList;
import static io.clownfish.clownfish.beans.SiteTreeBean.SAPCONNECTION;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.templatebeans.DatabaseTemplateBean;
import io.clownfish.clownfish.templatebeans.EmailTemplateBean;
import io.clownfish.clownfish.templatebeans.NetworkTemplateBean;
import io.clownfish.clownfish.templatebeans.SAPTemplateBean;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sulzbachr
 */
public class QuartzJob implements Job {
    @Autowired QuartzList quartzlist;
    @Autowired CfSiteService cfsiteService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateLoaderImpl freemarkerTemplateloader;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired TemplateUtil templateUtil;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired PropertyList propertylist;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    private @Getter @Setter List<CfSitedatasource> sitedatasourcelist;
    private @Getter @Setter Map<String, String> propertymap = null;
    private boolean sapSupport = false;
    private freemarker.template.Configuration freemarkerCfg;
    private SAPTemplateBean sapbean;
    private static SAPConnection sapc = null;
    private RPY_TABLE_READ rpytableread = null;
    private ClownfishConst.ViewModus modus = STAGING;
    final transient Logger logger = LoggerFactory.getLogger(QuartzJob.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        List<CfQuartz> joblist = quartzlist.getQuartzlist();
        joblist.stream().filter((quartz) -> (quartz.getName().compareToIgnoreCase(jec.getJobDetail().getKey().getName()) == 0)).map((quartz) -> {
            logger.info("JOB CLOWNFISH CMS Version 1.0: " + quartz.getName() + " - " + quartz.getSiteRef());
            return quartz;
        }).forEach((quartz) -> {
            callJob(quartz.getSiteRef().longValue());
        });
    }
    
    private void callJob(long siteref) {
        modus = STAGING;    // 1 = Staging mode (fetch sourcecode from commited repository) <= default
        // read all System Properties of the property table
        propertymap = propertylist.fillPropertyMap();
        //clownfishutil = new ClownfishUtil();
        String sapSupportProp = propertymap.get("sap_support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            sapc = new SAPConnection(SAPCONNECTION, "Clownfish1");
            rpytableread = new RPY_TABLE_READ(sapc);
        }
        
        // Freemarker Template
        freemarker.template.Template fmTemplate = null;
        Map fmRoot = null;

        // Velocity Template
        org.apache.velocity.VelocityContext velContext = null;
        org.apache.velocity.Template velTemplate = null;

        // fetch site by name or aliasname
        CfSite cfsite;
        cfsite = cfsiteService.findById(siteref);

        CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().longValue());
        // fetch the dependend template 
        if (0 == cftemplate.getScriptlanguage()) {  
            try {
                // Freemarker Template
                fmRoot = new LinkedHashMap();

                freemarkerCfg = new freemarker.template.Configuration();
                freemarkerCfg.setDefaultEncoding("UTF-8");
                freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                freemarkerCfg.setLocalizedLookup(false);
                freemarkerCfg.setLocale(Locale.GERMANY);

                fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
            } catch (MalformedTemplateNameException ex) {
                logger.error(ex.getMessage());
            } catch (ParseException ex) {
                logger.error(ex.getMessage());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        } else {                                    
            try {
                // Velocity Template
                velContext = new org.apache.velocity.VelocityContext();

                velTemplate = new org.apache.velocity.Template();
                org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                String templateContent;
                long currentTemplateVersion;
                try {
                    currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                } catch (NullPointerException ex) {
                    currentTemplateVersion = 0;
                }
                templateContent = templateUtil.getVersion(cftemplate.getId(), currentTemplateVersion);
                templateContent = templateUtil.fetchIncludes(templateContent, modus);
                StringReader reader = new StringReader(templateContent);
                velTemplate.setRuntimeServices(runtimeServices);
                velTemplate.setData(runtimeServices.parse(reader, cftemplate.getName()));
                velTemplate.initDocument();
            } catch (org.apache.velocity.runtime.parser.ParseException ex) {
                logger.error(ex.getMessage());
            }
        }

        // fetch the dependend datasources
        sitedatasourcelist = new ArrayList<>();
        sitedatasourcelist.addAll(cfsitedatasourceService.findBySiteref(cfsite.getId()));
        
        // write the output
        Writer out = new StringWriter();
        if (0 == cftemplate.getScriptlanguage()) {  // Freemarker template
            EmailTemplateBean emailbean = new EmailTemplateBean();
            emailbean.init(propertymap);
            if (null != fmRoot) {
                fmRoot.put("emailBean", emailbean);

                if (sapSupport) {
                    List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                    sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                    sapbean = new SAPTemplateBean();
                    fmRoot.put("sapBean", sapbean);
                }
                
                DatabaseTemplateBean databasebean = new DatabaseTemplateBean();
                databasebean.initjob(sitedatasourcelist, cfdatasourceService);
                fmRoot.put("databaseBean", databasebean);
                NetworkTemplateBean networkbean = new NetworkTemplateBean();
                fmRoot.put("networkBean", networkbean);

                fmRoot.put("property", propertymap);
                try {
                    if (null != fmTemplate) {
                        freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                        env.process();
                    }
                } catch (freemarker.template.TemplateException ex) {
                    logger.error(ex.getMessage());
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        } else {                                    // Velocity template
            EmailTemplateBean emailbean = new EmailTemplateBean();
            emailbean.init(propertymap);
            if (null != velContext) {
                velContext.put("emailBean", emailbean);
                if (sapSupport) {
                    List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                    sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                    sapbean = new SAPTemplateBean();
                    velContext.put("sapBean", sapbean);
                }
                DatabaseTemplateBean databasebean = new DatabaseTemplateBean();
                databasebean.initjob(sitedatasourcelist, cfdatasourceService);
                velContext.put("databaseBean", databasebean);
                NetworkTemplateBean networkbean = new NetworkTemplateBean();
                velContext.put("networkBean", networkbean);

                velContext.put("property", propertymap);
                if (null != velTemplate) {
                    velTemplate.merge(velContext, out);
                }
            }
        }
        System.out.println(out);
    }
    
}
