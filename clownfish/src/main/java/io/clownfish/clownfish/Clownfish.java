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
package io.clownfish.clownfish;

import com.google.common.reflect.ClassPath;
import io.clownfish.clownfish.exceptions.PageNotFoundException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.AssetList;
import io.clownfish.clownfish.beans.AttributContentList;
import io.clownfish.clownfish.templatebeans.*;
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.beans.QuartzList;
import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.HibernateInit;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfSearchhistory;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.jdbc.DatatableDeleteProperties;
import io.clownfish.clownfish.jdbc.DatatableNewProperties;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.DatatableUpdateProperties;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.lucene.LuceneConstants;
import io.clownfish.clownfish.lucene.SearchResult;
import io.clownfish.clownfish.lucene.Searcher;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSearchhistoryService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.util.ServerInfo;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author sulzbachr
 * Central class of the Clownfish server
 */
@RestController
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@Component
@Configuration
@PropertySources({
    @PropertySource("file:application.properties")
})
public class Clownfish {
    @Autowired CfSiteService cfsiteService;
    @Autowired CfSitecontentService cfsitecontentService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfListService cflistService;
    @Autowired CfSitelistService cfsitelistService;
    @Autowired @Getter @Setter CfAssetService cfassetService;
    @Autowired @Getter @Setter CfAttributcontentService cfattributcontentService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfJavascriptversionService cfjavascriptversionService;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    @Autowired TemplateUtil templateUtil;
    @Autowired PropertyList propertylist;
    @Autowired AttributContentList attributContentList;
    @Autowired AssetList assetList;
    @Autowired QuartzList quartzlist;
    @Autowired CfTemplateLoaderImpl freemarkerTemplateloader;
    @Autowired SiteUtil siteutil;
    @Autowired DatabaseUtil databaseUtil;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfSearchhistoryService cfsearchhistoryService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired private Scheduler scheduler;
    @Autowired private FolderUtil folderUtil;
    @Autowired Searcher searcher;
    @Autowired ConsistencyUtil consistencyUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired ServiceStatus servicestatus;
    
    DatabaseTemplateBean databasebean;
    EmailTemplateBean emailbean;
    SAPTemplateBean sapbean;
    NetworkTemplateBean networkbean;
    WebServiceTemplateBean webservicebean;
    ImportTemplateBean importbean;
    PDFTemplateBean pdfbean;

    private String contenttype;
    private String characterencoding;
    private String locale;

    private GzipSwitch gzipswitch;
    private freemarker.template.Configuration freemarkerCfg;
    private RPY_TABLE_READ rpytableread = null;
    private static SAPConnection sapc = null;
    private boolean sapSupport = false;
    private boolean jobSupport = false;
    private HttpSession userSession;
    private ClownfishConst.ViewModus modus = STAGING;
    private ClownfishUtil clownfishutil;
    private PropertyUtil propertyUtil;
    private MailUtil mailUtil;
    private DefaultUtil defaultUtil;
    private PDFUtil pdfUtil;
    private @Getter @Setter Map sitecontentmap;
    private @Getter @Setter Map searchcontentmap;
    private @Getter @Setter Map searchassetmap;
    private @Getter @Setter Map searchassetmetadatamap;
    private @Getter @Setter Map searchclasscontentmap;
    private @Getter @Setter Map searchmetadata;
    private @Getter @Setter MarkdownUtil markdownUtil;
    private @Getter @Setter ContentIndexer contentIndexer;
    private @Getter @Setter AssetIndexer assetIndexer;
    private @Getter @Setter int searchlimit;
    private @Getter @Setter Map<String, String> metainfomap;
    
    private Set<Class> templatebeans = null;
    private ArrayList<Class> loadabletemplatebeans = new ArrayList<>();
    
    final transient Logger LOGGER = LoggerFactory.getLogger(Clownfish.class);
    @Value("${bootstrap}") int bootstrap;
    @Value("${app.datasource.username}") String dbuser;
    @Value("${app.datasource.password}") String dbpassword;
    @Value("${app.datasource.url}") String dburl;
    @Value("${app.datasource.driverClassName}") String dbclass;
    @Value("${check.consistency:0}") int checkConsistency;
    @Value("${hibernate.init:0}") int hibernateInit;
    @Value("${sapconnection.file}") String SAPCONNECTION;
    
    /**
     * Call of the "root" site
     * Fetches the root site from the system property "site_root" and calls universalGet 
     * @param request
     * @param response
     */
    @RequestMapping("/")
    public void home(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String root_site = propertyUtil.getPropertyValue("site_root");
        if (null == root_site) {
            root_site = "root";
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, root_site);
        universalGet(root_site, request, response);
    }
    
    /**
     * Call of the "error" site
     * Fetches the error site from the system property "site_error" and calls universalGet 
     * @param request
     * @param response
     */
    @RequestMapping("/error")
    public void error(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String error_site = propertyUtil.getPropertyValue("site_error");
        if (null == error_site) {
            error_site = "error";
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, error_site);
        universalGet(error_site, request, response);
    }

    /**
     * Call of the "init" site
     * Init is called at the beginning of the Clownfish startup and after changing system properties
     * Checks the bootstrap flag of the application.properties and calls a bootstrap routine
     * Initializes several variables and starts Quartz job triggers
     */
    @PostConstruct
    public void init() {
        LOGGER.info("INIT CLOWNFISH START");
        servicestatus.setMessage("Clownfish is initializing");
        servicestatus.setOnline(false);
        try {
            templatebeans = findAllClassesInPackage("io.clownfish.clownfish.templatebeans");
            templatebeans.forEach(cl -> {
                    if (null != cl.getCanonicalName()) {
                        System.out.println(cl.getCanonicalName());
                        loadabletemplatebeans.add(cl);
                        /*
                        for (Method method : cl.getMethods()) {
                            System.out.println(method.getName());
                        }
                        */
                    }
                }
            );
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        if (1 == bootstrap) {
            bootstrap = 0;
            try {
                AnsiConsole.systemInstall();
                System.out.println(ansi().fg(GREEN));
                System.out.println("BOOTSTRAPPING II");
                System.out.println(ansi().reset());
                
                Properties props = new Properties();
                String propsfile = "application.properties";
                InputStream is = new FileInputStream(propsfile);
                if (null != is) {
                    props.load(is);
                    props.setProperty("bootstrap", String.valueOf(bootstrap));
                    File f = new File("application.properties");

                    bootstrap();

                    OutputStream out = new FileOutputStream(f);
                    DefaultPropertiesPersister p = new DefaultPropertiesPersister();
                    p.store(props, out, "Application properties");
                } else {
                    LOGGER.error("application.properties file not found");
                }
              } catch (IOException ex) {
                  LOGGER.error(ex.getMessage());
              }
        }
        
        /* Hazelcast test */
        /*
        if (null == hazelConfig) {
            hazelConfig = new Config();
        }
        if (null == hcInstance) {
            hcInstance = Hazelcast.newHazelcastInstance(hazelConfig);
        }
        */
        
        Package p = FacesContext.class.getPackage();
        if (null == clownfishutil) {
            clownfishutil = new ClownfishUtil();
        }
        
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        if ((new File("pom.xml")).exists()) {
            try {
                model = reader.read(new FileReader("pom.xml"));
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            } catch (IOException | XmlPullParserException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        if (null != model) {
            clownfishutil.setVersion(model.getVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        } else {
            clownfishutil.setVersion(getClass().getPackage().getImplementationVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        }
        try {
            AnsiConsole.systemInstall();
            System.out.println(ansi().fg(GREEN));
            System.out.println("INIT CLOWNFISH CMS Version " + clownfishutil.getVersion() + " on Tomcat " + clownfishutil.getVersionTomcat()+ " with Mojarra " + clownfishutil.getVersionMojarra());
            System.out.println(ansi().fg(RED));
            System.out.println("                               ...                                             ");
            System.out.println("                            &@@@@@@@                                           ");
            System.out.println("                          .&&%%%@%@@@@                                         ");
            System.out.println("                          %%%%%%%%&%%@                                         ");
            System.out.println("                         .%%%%%%%%#%%&                                         ");
            System.out.println("                         /%%%%%%%%%##%         &&@@.                           ");
            System.out.println("                    .,*//#############%        %#%#%@@                          ");
            System.out.println("                     #((###############       ###%#%%&@                         ");
            System.out.println("           //          #((((/(/(((((((/       (###(###&%                        ");
            System.out.println("        *((//((#/       #((((#(((((((#         (((##%##@                        ");
            System.out.println("      /(((((#(////#     .((((((((((((*         *((((###%                        ");
            System.out.println("     /(((((#((######     #(((((##((@@@@&        ((((((##                        ");
            System.out.println("    (((((((####@@&#(%     #((((####((((&@/      #(((((((*            (&@@&#     ");
            System.out.println("   /(((((((#######(((&    *((((##(((((((#@/     #(((((((/          &&%%%%&@@@,  ");
            System.out.println("  /((((((((((((((#((((&    (#((#(##((##((@@     #(#####(*       *########%%&@@@.");
            System.out.println("  /%#(((((((((((((/((((.   ###########(((#@*    ((#####(.      /#########%%%@@@@");
            System.out.println("   ,(((((((((((((((((((/   ###########((((@,   ((#######       ###########%%%@@@");
            System.out.println("    .(((((((((((((/((((*   (#########((((#@    ((######%      .##########%%%&@@@");
            System.out.println("       ,/(((((((((((((&   #(#########((((@/  .#((###((((       #########%%%%&@@@");
            System.out.println("           /((((((##(&   %(((((######(((&/  .#(((((((((.       *########%%%&&@@@");
            System.out.println("              ,//(#@   /(#(####(((##(((&.  (((((((((/           /######%%%&&@@@@");
            System.out.println("                ./* .#(########((#((*/.  *#(((((((###.           /%##%%%%@@@@@@ ");
            System.out.println("                /######((#######(    ./#######(###(#@              .&@@@@@@@%   ");
            System.out.println("               /#######%%#(((#((((@.  /@%#####(##((@#                           ");
            System.out.println("              /####%%#@& *#(((//(/(%@   *@@@@%#%&@@                             ");
            System.out.println("              %#%&&@&*    *((((///(@@&     .##/*                                ");
            System.out.println("                  ,,***,   *((/(#@@@@@,                                         ");
            System.out.println("                            *@@@@@@@@%                                          ");
            System.out.println(ansi().reset());
            
            // Check Consistence
            if (checkConsistency > 0) {
                consistencyUtil.checkConsistency();
            }
            
            // Generate Hibernate DOM Mapping
            HibernateInit hibernateInitializer = new HibernateInit(servicestatus, cfclassService, cfattributService, cfclasscontentService, cfattributcontentService, cflistcontentService, cfclasscontentkeywordService, cfkeywordService, dburl);
            hibernateUtil.init(hibernateInitializer);
            hibernateUtil.generateTablesDatamodel(hibernateInit);
            // generate Relation Tables
            hibernateUtil.generateRelationsDatamodel(hibernateInit);
            
            // read all System Properties of the property table
            if (null == propertyUtil) {
                propertyUtil = new PropertyUtil(propertylist);
            }
            propertylist.setClownfish(this);
            if (null == defaultUtil) {
                defaultUtil = new DefaultUtil();
            }

            if (mailUtil == null)
                mailUtil = new MailUtil(propertyUtil);

            if (pdfUtil == null)
                pdfUtil = new PDFUtil(cftemplateService, cftemplateversionService, cfsitedatasourceService, cfdatasourceService, cfsiteService, propertyUtil, templateUtil);

            // Set default values
            modus = STAGING;    // 1 = Staging mode (fetch sourcecode from commited repository) <= default
                                // 0 = Development mode (fetch sourcecode from database)
            defaultUtil.setCharacterEncoding("UTF-8")
                       .setContentType("text/html")
                       .setLocale(new Locale("de"));
            
            sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
            if (sapSupport) {
                if (null == sapc) {
                    sapc = new SAPConnection(SAPCONNECTION, "Clownfish1");
                    rpytableread = new RPY_TABLE_READ(sapc);
                }
            }
            // Override default values with system properties
            String systemContentType = propertyUtil.getPropertyValue("response_contenttype");
            String systemCharacterEncoding = propertyUtil.getPropertyValue("response_characterencoding");
            String systemLocale = propertyUtil.getPropertyValue("response_locale");
            if (!systemCharacterEncoding.isEmpty()) {
                defaultUtil.setCharacterEncoding(systemCharacterEncoding);
            }
            if (!systemContentType.isEmpty()) {
                defaultUtil.setContentType(systemContentType);
            }
            if (!systemLocale.isEmpty()) {
                defaultUtil.setLocale(new Locale(systemLocale));
            }
            if (null == gzipswitch) {
                gzipswitch = new GzipSwitch();
            }
            
            if (null == markdownUtil) {
                markdownUtil = new MarkdownUtil();
            }
            if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getIndex_folder().isEmpty())) {
                // Call a parallel thread to index the content in Lucene
                if (null == contentIndexer) {
                    contentIndexer = new ContentIndexer(cfattributcontentService, indexService);
                }
                contentIndexer.run();
                LOGGER.info("CONTENTINDEXER RUN");
                if (null == assetIndexer) {
                    assetIndexer = new AssetIndexer(cfassetService, indexService, propertylist);
                }
                assetIndexer.run();
                indexService.getWriter().commit();
            }
           
            // Init Site Metadata Map
            if (null == metainfomap) {
                //hzMetainfomap = hcInstance.getMap("metainfos");
                metainfomap = new HashMap();
            }
            metainfomap.put("version", clownfishutil.getVersion());
            metainfomap.put("versionMojarra", clownfishutil.getVersionMojarra());
            metainfomap.put("versionTomcat", clownfishutil.getVersionTomcat());
            
            // Init Lucene Search Map
            if (null == searchcontentmap) {
                searchcontentmap = new HashMap<>();
            }
            if (null == searchassetmap) {
                searchassetmap = new HashMap<>();
            }
            if (null == searchassetmetadatamap) {
                searchassetmetadatamap = new HashMap<>();
            }
            if (null == searchclasscontentmap) {
                searchclasscontentmap = new HashMap<>();
            }
            if (null == searchmetadata) {
                searchmetadata = new HashMap<>();
            }
            searchlimit = propertyUtil.getPropertyInt("lucene_searchlimit", LuceneConstants.MAX_SEARCH);
            
            jobSupport = propertyUtil.getPropertyBoolean("job_support", jobSupport);
            if (jobSupport) {
                scheduler.clear();
                // Fetch the Quartz jobs
                quartzlist.init();
                quartzlist.setClownfish(this);
                List<CfQuartz> joblist = quartzlist.getQuartzlist();
                joblist.stream().forEach((quartz) -> {
                    try {
                        if (quartz.isActive()) {
                            JobDetail job = newJob(quartz.getName());
                            scheduler.scheduleJob(job, trigger(job, quartz.getSchedule()));
                            System.out.println(ansi().fg(GREEN));
                            System.out.println("JOB SCHEDULE: " + quartz.getName());
                            System.out.println(ansi().reset());

                        }
                    } catch (SchedulerException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                });
            }
            AnsiConsole.systemUninstall();
        } catch (SchedulerException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        folderUtil.init();
        servicestatus.setMessage("Clownfish is online");
        servicestatus.setOnline(true);
        LOGGER.info("INIT CLOWNFISH END");
    }

    public Clownfish() {
    }
    
    @PostMapping(path = "/search")
    public void postsearch(@Context HttpServletRequest request, @Context HttpServletResponse response) throws ParseException {
        try {
            userSession = request.getSession();
            String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Gson gson = new Gson();
            List<JsonFormParameter> map;
            map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
            
            Map parametermap = clownfishutil.getParametermap(map);
            
            String[] searchexpressions = parametermap.get("searchparam").toString().split(" ");
            updateSearchhistory(searchexpressions);
            
            searcher.setIndexPath(folderUtil.getIndex_folder());
            searcher.setPropertyList(propertylist);
            long startTime = System.currentTimeMillis();
            SearchResult searchresult = searcher.search(parametermap.get("searchparam").toString(), searchlimit);
            long endTime = System.currentTimeMillis();
            
            LOGGER.info("Search Time :" + (endTime - startTime));
            searchmetadata.clear();
            searchmetadata.put("cfSearchQuery", parametermap.get("searchparam").toString());
            searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
            searchcontentmap.clear();
            searchresult.getFoundSites().stream().forEach((site) -> {
                searchcontentmap.put(site.getName(), site);
            });
            searchassetmap.clear();
            searchresult.getFoundAssets().stream().forEach((asset) -> {
                searchassetmap.put(asset.getName(), asset);
            });
            searchassetmetadatamap.clear();
            searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
            });
            searchclasscontentmap.clear();
            searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
            });
            
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, search_site);
            universalGet(search_site, request, response);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }    
    }
    
    /**
     * Call of the "search" site
     * Fetches the search site from the system property "site_search" and calls universalGet
     * Instantiates the Searcher class
     * Clears and fills the searchmetadata
     * @param query
     * @param request
     * @param response
     */
    @GetMapping(path = "/search/{query}")
    public void search(@PathVariable("query") String query, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            String[] searchexpressions = query.split(" ");
            updateSearchhistory(searchexpressions);
            
            searcher.setIndexPath(folderUtil.getIndex_folder());
            long startTime = System.currentTimeMillis();
            SearchResult searchresult = searcher.search(query, searchlimit);
            long endTime = System.currentTimeMillis();
            
            LOGGER.info("Search Time :" + (endTime - startTime));
            searchmetadata.clear();
            searchmetadata.put("cfSearchQuery", query);
            searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
            searchcontentmap.clear();
            searchresult.getFoundSites().stream().forEach((site) -> {
                searchcontentmap.put(site.getName(), site);
            });
            searchassetmap.clear();
            searchresult.getFoundAssets().stream().forEach((asset) -> {
                searchassetmap.put(asset.getName(), asset);
            });
            searchassetmetadatamap.clear();
            searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
            });
            searchclasscontentmap.clear();
            searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
            });
            
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, search_site);
            universalGet(search_site, request, response);
        } catch (IOException | ParseException ex) {
            LOGGER.error(ex.getMessage());
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            universalGet(search_site, request, response);
        }
    }
    
    @GetMapping(path = "/{name}.css")
    public void universalGetCSS(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        CfStylesheet cfstylesheet = null;
        try {
            cfstylesheet = cfstylesheetService.findByName(name);
            response.setContentType("text/css");
            response.setCharacterEncoding("UTF-8");
            PrintWriter outwriter = response.getWriter();
            outwriter.println(cfstylesheet.getContent());
        } catch (Exception ex) {
            System.out.println("CSS NOT FOUND");
        }
    }
    
    @GetMapping(path = {"/{name}.tpl", "/{name}.html"})
    public void universalGetTemplate(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        CfTemplate cftemplate = null;
        try {
            cftemplate = cftemplateService.findByName(name);
            if (2 == cftemplate.getScriptlanguage()) {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                PrintWriter outwriter = response.getWriter();
                outwriter.println(cftemplate.getContent());
            } else {
                LOGGER.warn("ONLY HTML Templates");
            }
        } catch (Exception ex) {
            LOGGER.warn("Template NOT FOUND");
        }
    }
    
    @GetMapping(path = "/{name}.js")
    public void universalGetJS(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        CfJavascript cfjavascript = null;
        try {
            cfjavascript = cfjavascriptService.findByName(name);
            response.setContentType("application/javascript");
            response.setCharacterEncoding("UTF-8");
            PrintWriter outwriter = response.getWriter();
            outwriter.println(cfjavascript.getContent());
        } catch (Exception ex) {
            System.out.println("JS NOT FOUND");
        }
    }
    
    @GetMapping(path = "/robots.txt")
    public void universalGetRobots(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        CfTemplate cftemplate = null;
        try {
            cftemplate = cftemplateService.findByName("robots");
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter outwriter = response.getWriter();
            outwriter.print(cftemplate.getContent());
        } catch (Exception ex) {
            System.out.print("ROBOTS NOT FOUND");
        }
    }
    
    /**
     * GET
     * 
     * @param name
     * @param request
     * @param response
     */
    @GetMapping(path = "/{name}/**")
    public void universalGet(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        if (servicestatus.isOnline()) {
            try {
                ArrayList urlParams = new ArrayList();
                if (0 != name.compareToIgnoreCase("searchresult")) {
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    
                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                urlParams.add(params[i]);
                            }
                        }
                    }
                    
                    if (name.compareToIgnoreCase(path) != 0) {
                        name = path.substring(1);
                        if (name.lastIndexOf("/")+1 == name.length()) {
                            name = name.substring(0, name.length()-1);
                        }
                    }
                }

                userSession = request.getSession();
                Map<String, String[]> querymap = request.getParameterMap();

                ArrayList queryParams = new ArrayList();
                querymap.keySet().stream().map((key) -> {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName((String) key);
                    String[] values = querymap.get((String) key);
                    jfp.setValue(values[0]);
                    return jfp;
                }).forEach((jfp) -> {
                    queryParams.add(jfp);
                });

                addHeader(response, clownfishutil.getVersion());
                Future<ClownfishResponse> cfResponse = makeResponse(name, queryParams, urlParams, false);
                if (cfResponse.get().getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                }
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.get().getOutput().getBytes(this.characterencoding)); 
            } catch (IOException | InterruptedException | ExecutionException ex) {
                LOGGER.error(ex.getMessage());
            } catch (PageNotFoundException ex) {
                String error_site = propertyUtil.getPropertyValue("site_error");
                if (null == error_site) {
                    error_site = "error";
                }
                request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, error_site);
                universalGet(error_site, request, response);
            }
        } else {
            PrintWriter outwriter = null;
            try {
                outwriter = response.getWriter();
                outwriter.println(servicestatus.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                if (null != outwriter) {
                    outwriter.close();
                }
            }
        }
    }

    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     */
    @PostMapping("/{name}/**")
    public void universalPost(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (path.contains("/")) {
                String[] params = path.split("/");
                for (int i = 1; i < params.length; i++) {
                    if (1 == i) {
                        path = params[i];
                    } else {
                        urlParams.add(params[i]);
                    }
                }
            }
            if (name.compareToIgnoreCase(path) != 0) {
                name = path.substring(1);
                if (name.lastIndexOf("/")+1 == name.length()) {
                    name = name.substring(0, name.length()-1);
                }
            }
            
            userSession = request.getSession();
            if (request.getContentType().startsWith("multipart/form-data")) {
                LOGGER.info("MULTIPART");
            } else {
                String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                Gson gson = new Gson();
                List<JsonFormParameter> map;
                map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
                addHeader(response, clownfishutil.getVersion());
                Future<ClownfishResponse> cfResponse = makeResponse(name, map, urlParams, false);
                if (cfResponse.get().getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.get().getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.get().getOutput().getBytes(this.characterencoding)); 
                }
            }
        } catch (IOException | InterruptedException | ExecutionException | PageNotFoundException | IllegalStateException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * makeResponse
     * 
     * @param name
     * @param postmap
     * @param urlParams
     * @param makestatic
     * @return 
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException 
     */
    @Async
    public Future<ClownfishResponse> makeResponse(String name, List<JsonFormParameter> postmap, List urlParams, boolean makestatic) throws PageNotFoundException {
        ClownfishResponse cfresponse = new ClownfishResponse();
        
        try {
            List<CfSitedatasource> sitedatasourcelist;
            // Freemarker Template
            freemarker.template.Template fmTemplate = null;
            Map fmRoot = null;

            // Velocity Template
            org.apache.velocity.VelocityContext velContext = null;
            org.apache.velocity.Template velTemplate = null;

            // fetch parameter list
            Map parametermap = clownfishutil.getParametermap(postmap);
            if (parametermap.containsKey("modus")) {    // check mode for display (staging or dev)
                if (parametermap.get("modus").toString().compareToIgnoreCase("dev") == 0) {
                    modus = DEVELOPMENT;
                }
            }

            // fetch site by name or aliasname
            CfSite cfsite = null;
            try {
                cfsite = cfsiteService.findByName(name);
            } catch (Exception ex) {
                try {
                    cfsite = cfsiteService.findByAliaspath(name);
                } catch (Exception e1) {
                    throw new PageNotFoundException("PageNotFound Exception: " + name);
                }
            }
            // Site has not job flag
            if (!cfsite.isJob()) {
                // increment site hitcounter
                long hitcounter = cfsite.getHitcounter().longValue();
                cfsite.setHitcounter(BigInteger.valueOf(hitcounter+1));
                cfsiteService.edit(cfsite);
                
                // Site has static flag
                if ((cfsite.isStaticsite()) && (!makestatic)) {
                    if ((cfsite.getContenttype() != null)) {
                        if (!cfsite.getContenttype().isEmpty()) {
                            this.contenttype = cfsite.getContenttype();
                        }
                    }
                    if ((cfsite.getCharacterencoding() != null)) {
                        if (!cfsite.getCharacterencoding().isEmpty()) {
                            this.characterencoding = cfsite.getCharacterencoding();
                        }
                    }
                    if ((cfsite.getLocale() != null)) {
                        if (!cfsite.getLocale().isEmpty()) {
                            this.locale = cfsite.getLocale();
                        }
                    }
                    
                    cfresponse = getStaticSite(name);
                    if (0 == cfresponse.getErrorcode()) {
                        return new AsyncResult<>(cfresponse);
                    } else {
                        Future<ClownfishResponse> cfStaticResponse = makeResponse(name, postmap, urlParams, true);
                        try {
                            generateStaticSite(name, cfStaticResponse.get().getOutput());
                            return makeResponse(name, postmap, urlParams, false);
                        } catch (InterruptedException | ExecutionException ex) {
                            LOGGER.error(ex.getMessage());
                            return makeResponse(name, postmap, urlParams, false);
                        }
                    }
                } else {
                    if ((cfsite.getContenttype() != null)) {
                        if (!cfsite.getContenttype().isEmpty()) {
                            this.contenttype = cfsite.getContenttype();
                        }
                    }
                    if ((cfsite.getCharacterencoding() != null)) {
                        if (!cfsite.getCharacterencoding().isEmpty()) {
                            this.characterencoding = cfsite.getCharacterencoding();
                        }
                    }
                    if ((cfsite.getLocale() != null)) {
                        if (!cfsite.getLocale().isEmpty()) {
                            this.locale = cfsite.getLocale();
                        }
                    }

                    try {
                        CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().longValue());
                        // fetch the dependend template 
                        switch (cftemplate.getScriptlanguage()) {
                            case 0:
                                fmRoot = new LinkedHashMap();
                                freemarkerTemplateloader.setModus(modus);

                                freemarkerCfg = new freemarker.template.Configuration();
                                freemarkerCfg.setDefaultEncoding("UTF-8");
                                freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                                freemarkerCfg.setLocalizedLookup(false);
                                freemarkerCfg.setLocale(Locale.GERMANY);

                                fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                                break;
                            case 1:
                                velContext = new org.apache.velocity.VelocityContext();

                                velTemplate = new org.apache.velocity.Template();
                                org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                                String templateContent;
                                if (DEVELOPMENT == modus) {
                                    templateContent = cftemplate.getContent();
                                } else {
                                    long currentTemplateVersion;
                                    try {
                                        currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                                    } catch (NullPointerException ex) {
                                        currentTemplateVersion = 0;
                                    }
                                    templateContent = templateUtil.getVersion(cftemplate.getId(), currentTemplateVersion);
                                }
                                templateContent = templateUtil.fetchIncludes(templateContent, modus);
                                StringReader reader = new StringReader(templateContent);
                                velTemplate.setRuntimeServices(runtimeServices);
                                velTemplate.setData(runtimeServices.parse(reader, cftemplate.getName()));
                                velTemplate.initDocument();
                                break;
                            default:
                                break;
                        }

                        String gzip = propertyUtil.getPropertySwitch("html_gzip", cfsite.getGzip());
                        if (gzip.compareToIgnoreCase("on") == 0) {
                            gzipswitch.setGzipon(true);
                        }
                        String htmlcompression = propertyUtil.getPropertySwitch("html_compression", cfsite.getHtmlcompression());
                        HtmlCompressor htmlcompressor = new HtmlCompressor();
                        htmlcompressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
                        htmlcompressor.setPreserveLineBreaks(false);
                        // fetch the dependend stylesheet, if available
                        String cfstylesheet = "";
                        if (cfsite.getStylesheetref() != null) {
                            cfstylesheet = ((CfStylesheet) cfstylesheetService.findById(cfsite.getStylesheetref().longValue())).getContent();
                            if (htmlcompression.compareToIgnoreCase("on") == 0) {
                                htmlcompressor.setCompressCss(true);
                                cfstylesheet = htmlcompressor.compress(cfstylesheet);
                            }
                        }

                        // fetch the dependend javascript, if available
                        String cfjavascript = "";
                        if (cfsite.getJavascriptref() != null) {
                            cfjavascript = ((CfJavascript) cfjavascriptService.findById(cfsite.getJavascriptref().longValue())).getContent();
                            if (htmlcompression.compareToIgnoreCase("on") == 0) {
                                htmlcompressor.setCompressJavaScript(true);
                                cfjavascript = htmlcompressor.compress(cfjavascript);
                            }
                        }

                        // fetch the dependend content
                        List<CfSitecontent> sitecontentlist = new ArrayList<>();
                        sitecontentlist.addAll(cfsitecontentService.findBySiteref(cfsite.getId()));
                        sitecontentmap = siteutil.getSitecontentmapList(sitecontentlist);

                        // fetch the dependend datalists, if available
                        sitecontentmap = siteutil.getSitelist_list(cfsite, sitecontentmap);
                        
                        // fetch the site assetlibraries
                        sitecontentmap = siteutil.getSiteAssetlibrary(cfsite, sitecontentmap);
                        
                        // fetch the site keywordlibraries
                        sitecontentmap = siteutil.getSiteKeywordlibrary(cfsite, sitecontentmap);

                        // manage parameters 
                        HashMap<String, DatatableProperties> datatableproperties = clownfishutil.getDatatableproperties(postmap);
                        EmailProperties emailproperties = clownfishutil.getEmailproperties(postmap);
                        HashMap<String, DatatableNewProperties> datatablenewproperties = clownfishutil.getDatatablenewproperties(postmap);
                        HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = clownfishutil.getDatatabledeleteproperties(postmap);
                        HashMap<String, DatatableUpdateProperties> datatableupdateproperties = clownfishutil.getDatatableupdateproperties(postmap);
                        manageSessionVariables(postmap);
                        writeSessionVariables(parametermap);

                        // fetch the dependend datasources
                        sitedatasourcelist = new ArrayList<>();
                        sitedatasourcelist.addAll(cfsitedatasourceService.findBySiteref(cfsite.getId()));

                        HashMap<String, HashMap> dbexport = databaseUtil.getDbexport(sitedatasourcelist, datatableproperties, datatablenewproperties, datatabledeleteproperties, datatableupdateproperties);
                        sitecontentmap.put("db", dbexport);
                        // Put meta info to sitecontentmap
                        metainfomap.put("title", cfsite.getTitle());
                        metainfomap.put("description", cfsite.getDescription());
                        metainfomap.put("name", cfsite.getName());
                        metainfomap.put("encoding", cfsite.getCharacterencoding());
                        metainfomap.put("contenttype", cfsite.getContenttype());
                        metainfomap.put("locale", cfsite.getLocale());
                        metainfomap.put("alias", cfsite.getAliaspath());

                        // send a mail, if email properties are set
                        if (emailproperties != null) {
                            try {
                                sendRespondMail(emailproperties.getSendto(), emailproperties.getSubject(), emailproperties.getBody());
                            } catch (Exception ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }

                        // instantiate Template Beans
                        networkbean = new NetworkTemplateBean();
                        webservicebean = new WebServiceTemplateBean();
                        
                        emailbean = new EmailTemplateBean();
                        emailbean.init(propertyUtil.getPropertymap(), mailUtil, propertyUtil);
                        
                        if (sapSupport) {
                            List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                            sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                            sapbean = new SAPTemplateBean();
                            sapbean.init(sapc, sitesaprfclist, rpytableread, postmap);
                        }
                        
                        databasebean = new DatabaseTemplateBean();
                        importbean = new ImportTemplateBean();
                        pdfbean = new PDFTemplateBean();
                        pdfbean.init(pdfUtil);
                        if (!sitedatasourcelist.isEmpty()) {
                            databasebean.init(sitedatasourcelist, cfdatasourceService);
                            importbean.init(sitedatasourcelist, cfdatasourceService);
                        }
                        
                        // write the output
                        Writer out = new StringWriter();
                        switch (cftemplate.getScriptlanguage()) {
                            case 0:                                             // FREEMARKER
                                if (null != fmRoot) {
                                    fmRoot.put("css", cfstylesheet);
                                    fmRoot.put("js", cfjavascript);
                                    fmRoot.put("sitecontent", sitecontentmap);
                                    fmRoot.put("metainfo", metainfomap);
                                    fmRoot.put("property", propertyUtil.getPropertymap());

                                    fmRoot.put("emailBean", emailbean);
                                    if (sapSupport) {
                                        fmRoot.put("sapBean", sapbean);
                                    }
                                    fmRoot.put("databaseBean", databasebean);
                                    fmRoot.put("importBean", importbean);
                                    fmRoot.put("networkBean", networkbean);
                                    fmRoot.put("webserviceBean", webservicebean);
                                    fmRoot.put("pdfBean", pdfbean);

                                    fmRoot.put("parameter", parametermap);
                                    if (!searchmetadata.isEmpty()) {
                                        fmRoot.put("searchmetadata", searchmetadata);
                                    }
                                    if (!searchcontentmap.isEmpty()) {
                                        fmRoot.put("searchcontentlist", searchcontentmap);
                                    }
                                    if (!searchassetmap.isEmpty()) {
                                        fmRoot.put("searchassetlist", searchassetmap);
                                    }
                                    if (!searchassetmetadatamap.isEmpty()) {
                                        fmRoot.put("searchassetmetadatalist", searchassetmetadatamap);
                                    }
                                    if (!searchclasscontentmap.isEmpty()) {
                                        fmRoot.put("searchclasscontentlist", searchclasscontentmap);
                                    }
                                    try {
                                        if (null != fmTemplate) {
                                            freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                                            env.process();
                                        }
                                    } catch (freemarker.template.TemplateException ex) {
                                        LOGGER.error(ex.getMessage());
                                    }
                                }
                                break;
                            case 1:                                             // VELOCITY
                                if (null != velContext) {
                                    velContext.put("css", cfstylesheet);
                                    velContext.put("js", cfjavascript);
                                    velContext.put("sitecontent", sitecontentmap);
                                    velContext.put("metainfo", metainfomap);

                                    velContext.put("emailBean", emailbean);
                                    if (sapSupport) {
                                        velContext.put("sapBean", sapbean);
                                    }
                                    velContext.put("databaseBean", databasebean);
                                    velContext.put("importBean", importbean);
                                    velContext.put("networkBean", networkbean);
                                    velContext.put("webserviceBean", webservicebean);
                                    velContext.put("pdfBean", pdfbean);

                                    velContext.put("parameter", parametermap);
                                    velContext.put("property", propertyUtil.getPropertymap());
                                    if (!searchmetadata.isEmpty()) {
                                        velContext.put("searchmetadata", searchmetadata);
                                    }
                                    if (!searchcontentmap.isEmpty()) {
                                        velContext.put("searchcontentlist", searchcontentmap);
                                    }
                                    if (!searchassetmap.isEmpty()) {
                                        velContext.put("searchassetlist", searchassetmap);
                                    }
                                    if (!searchassetmetadatamap.isEmpty()) {
                                        velContext.put("searchassetmetadatalist", searchassetmetadatamap);
                                    }
                                    if (!searchclasscontentmap.isEmpty()) {
                                        velContext.put("searchclasscontentlist", searchclasscontentmap);
                                    }
                                    if (null != velTemplate) {
                                        velTemplate.merge(velContext, out);
                                    }
                                }
                                break;
                            default:                                            // HTML
                        }
                        
                        if (htmlcompression.compareToIgnoreCase("on") == 0) {
                            htmlcompressor.setCompressCss(false);
                            htmlcompressor.setCompressJavaScript(false);

                            cfresponse.setErrorcode(0);
                            cfresponse.setOutput(htmlcompressor.compress(out.toString()));
                            //LOGGER.info("END makeResponse: " + name);
                            return new AsyncResult<>(cfresponse);
                        } else {
                            cfresponse.setErrorcode(0);
                            cfresponse.setOutput(out.toString());
                            //LOGGER.info("END makeResponse: " + name);
                            return new AsyncResult<>(cfresponse);
                        }
                    } catch (NoResultException ex) {
                        LOGGER.info("Exception: " + ex);
                        cfresponse.setErrorcode(1);
                        cfresponse.setOutput("No template");
                        //LOGGER.info("END makeResponse: " + name);
                        return new AsyncResult<>(cfresponse);
                    }
                }
            } else {
                cfresponse.setErrorcode(2);
                cfresponse.setOutput("Only for Job calling");
                return new AsyncResult<>(cfresponse);
            }
        } catch (IOException | org.apache.velocity.runtime.parser.ParseException ex) {
            cfresponse.setErrorcode(1);
            cfresponse.setOutput(ex.getMessage());
            //LOGGER.info("END makeResponse: " + name);
            return new AsyncResult<>(cfresponse);
        }
    }
    
    /**
     * sendRespondMail
     * 
     */
    private void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        MailUtil mailutil = new MailUtil(propertyUtil);
        mailutil.sendRespondMail(mailto, subject, mailbody);
    }

    /**
     * manageSessionVariables
     * 
     */
    private void manageSessionVariables(List<JsonFormParameter> postmap) {
        if (postmap != null) {
            postmap.stream().filter((jfp) -> (jfp.getName().startsWith("session"))).forEach((jfp) -> {
                userSession.setAttribute(jfp.getName(), jfp.getValue());
            });
        }
    }

    /**
     * writeSessionVariables
     * 
     */
    private void writeSessionVariables(Map parametermap) {
        Collections.list(userSession.getAttributeNames()).stream().filter((key) -> (key.startsWith("session"))).forEach((key) -> {
            String attributevalue = (String) userSession.getAttribute(key);
            parametermap.put(key, attributevalue);
        });
    }

    /**
     * addHeader
     * 
     */
    private void addHeader(HttpServletResponse response, String version) {
        response.addHeader("Server", "Clownfish Server Open Source Version " + version);
        response.addHeader("X-Powered-By", "Clownfish Server Open Source Version " + version + " by Rainer Sulzbach");
    }

    /**
     * newJob
     * 
     */
    private JobDetail newJob(String identity) {
        return JobBuilder.newJob().ofType(QuartzJob.class).storeDurably()
            .withIdentity(JobKey.jobKey(identity))
            .build();
    }

    /**
     * trigger
     * 
     */
    private CronTrigger trigger(JobDetail jobDetail, String schedule) {
        return TriggerBuilder.newTrigger().forJob(jobDetail)
            .withIdentity(jobDetail.getKey().getName(), jobDetail.getKey().getGroup())
            .withSchedule(cronSchedule(schedule))
            .build();
    }
    
    /**
     * getStaticSite
     * 
     */
    private ClownfishResponse getStaticSite(String sitename) {
        ClownfishResponse cfResponse = new ClownfishResponse();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(folderUtil.getStatic_folder() + File.separator + sitename), "UTF-8"));
            StringBuilder sb = new StringBuilder(1024);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            cfResponse.setOutput(sb.toString());
            cfResponse.setErrorcode(0);
            br.close();
            return cfResponse;
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            cfResponse.setOutput("Static site not found");
            cfResponse.setErrorcode(1);
            
            return cfResponse;
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    /**
     * generateStaticSite
     * 
     */
    private void generateStaticSite(String sitename, String content) {
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(new File(folderUtil.getStatic_folder()+ File.separator + sitename));
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
            try {
                writer.write(content);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create the destination file", e);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != fileStream) {
                    fileStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    /**
     * bootstrap
     * 
     */
    private void bootstrap() throws FileNotFoundException {
        List<CfTemplate> cftemplatelist = cftemplateService.findAll();
        for (CfTemplate template : cftemplatelist) {
            try {
                templateUtil.setTemplateContent(template.getContent());

                String content = templateUtil.getTemplateContent();
                byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));

                templateUtil.setCurrentVersion(1);
                templateUtil.writeVersion(template.getId(), templateUtil.getCurrentVersion(), output, 0);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    /**
     * updateSearchhistory
     * 
     */
    private void updateSearchhistory(String[] searchexpressions) {
        for (String expression : searchexpressions) {
            if ((expression.length() >= 3) && (!expression.endsWith("*"))) {
                try {
                    CfSearchhistory searchhistory = cfsearchhistoryService.findByExpression(expression.toLowerCase());
                    searchhistory.setCounter(searchhistory.getCounter()+1);
                    cfsearchhistoryService.edit(searchhistory);
                } catch (NoResultException ex) {
                    CfSearchhistory newsearchhistory = new CfSearchhistory();
                    newsearchhistory.setExpression(expression.toLowerCase());
                    newsearchhistory.setCounter(1);
                    cfsearchhistoryService.create(newsearchhistory);
                }
            }
        }
    }
    
    private Set<Class> findAllClassesInPackage(String packageName) throws IOException {
        return ClassPath.from(ClassLoader.getSystemClassLoader())
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName()
                .equalsIgnoreCase(packageName))
                .map(clazz -> clazz.load())
                .collect(Collectors.toSet());
    }
}
