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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import de.destrukt.sapconnection.SAPConnection;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import io.clownfish.clownfish.beans.*;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.compiler.CfClassLoader;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_SITE;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.HibernateInit;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.exceptions.PageNotFoundException;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.jdbc.DatatableDeleteProperties;
import io.clownfish.clownfish.jdbc.DatatableNewProperties;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.DatatableUpdateProperties;
import io.clownfish.clownfish.lucene.*;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.templatebeans.*;
import io.clownfish.clownfish.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.util.ServerInfo;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fusesource.jansi.AnsiConsole;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.datamodels.CfDiv;
import io.clownfish.clownfish.datamodels.CfLayout;
import io.clownfish.clownfish.datamodels.ClientInformation;
import io.clownfish.clownfish.exceptions.ClownfishTemplateException;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.serviceimpl.CfStringTemplateLoaderImpl;
import io.clownfish.clownfish.websocket.WebSocketServer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.apache.commons.fileupload.FileItem;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primefaces.webapp.MultipartRequest;
import static org.quartz.CronScheduleBuilder.cronSchedule;


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
    @Autowired @Getter @Setter CfAssetService cfassetService;
    @Autowired @Getter @Setter CfAttributcontentService cfattributcontentService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfJavaService cfjavaService;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    @Autowired CfLayoutcontentService cflayoutcontentService;
    @Autowired CfStaticsiteService cfstaticsiteservice;
    @Autowired TemplateUtil templateUtil;
    @Autowired PropertyList propertylist;
    @Autowired QuartzList quartzlist;
    @Autowired CfTemplateLoaderImpl freemarkerTemplateloader;
    @Autowired CfStringTemplateLoaderImpl freemarkerStringTemplateloader;
    @Autowired SiteUtil siteutil;
    @Autowired DatabaseUtil databaseUtil;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfKeywordlistService cfkeywordlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired private Scheduler scheduler;
    @Autowired private FolderUtil folderUtil;
    @Autowired Searcher searcher;
    @Autowired ConsistencyUtil consistencyUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired ServiceStatus servicestatus;
    @Autowired SearchUtil searchUtil;
    @Autowired CfSearchdatabaseService cfsearchdatabaseService;
    @Autowired AccessManagerUtil accessmanager;
    @Autowired ContentUtil contentUtil;
    
    DatabaseTemplateBean databasebean;
    ContentTemplateBean contentbean;
    EmailTemplateBean emailbean;
    SAPTemplateBean sapbean;
    NetworkTemplateBean networkbean;
    WebServiceTemplateBean webservicebean;
    WebSocketTemplateBean websocketbean;
    UploadTemplateBean uploadbean;
    ImportTemplateBean importbean;
    PDFTemplateBean pdfbean;
    ExternalClassProvider externalclassproviderbean;
    CfClassCompiler cfclassCompiler;
    CfClassLoader cfclassLoader;
    @Autowired AuthTokenList authtokenlist;
    AuthTokenListClasscontent authtokenlistclasscontent = null;

    private String contenttype;
    private String characterencoding;
    private String locale;

    private GzipSwitch gzipswitch;
    private freemarker.template.Configuration freemarkerCfg;
    private @Getter @Setter RPY_TABLE_READ rpytableread = null;
    private @Getter @Setter RFC_GET_FUNCTION_INTERFACE rfcfunctioninterface = null;
    private @Getter @Setter RFC_GROUP_SEARCH rfcgroupsearch = null;
    private @Getter @Setter RFC_FUNCTION_SEARCH rfcfunctionsearch = null;
    private static SAPConnection sapc = null;
    private boolean sapSupport = false;
    private boolean jobSupport = false;
    private HttpSession userSession;
    private ClownfishConst.ViewModus modus = STAGING;
    private boolean preview = false;
    private boolean cf_job = false;
    private ClownfishUtil clownfishutil;
    private PropertyUtil propertyUtil;
    private MailUtil mailUtil;
    private DefaultUtil defaultUtil;
    private PDFUtil pdfUtil;
    private BeanUtil beanUtil;
    private ClassPathUtil classpathUtil;
    private MavenList mavenlist;
    private StaticSiteUtil staticSiteUtil;
    private @Getter @Setter Map sitecontentmap;
    private @Getter @Setter Map searchcontentmap;
    private @Getter @Setter Map searchassetmap;
    private @Getter @Setter Map searchassetmetadatamap;
    private @Getter @Setter Map searchclasscontentmap;
    private @Getter @Setter Map searchmetadata;
    private @Getter @Setter MarkdownUtil markdownUtil;
    private @Getter @Setter ContentIndexer contentIndexer;
    private @Getter @Setter AssetIndexer assetIndexer;
    private @Getter @Setter DatabasetableIndexer databasetableIndexer;
    private @Getter @Setter int searchlimit;
    private @Getter @Setter Map<String, String> metainfomap;
    private static HibernateInit hibernateInitializer = null;
        
    private WebSocketServer wss;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(Clownfish.class);
    @Value("${bootstrap}") int bootstrap;
    @Value("${app.datasource.username}") String dbuser;
    @Value("${app.datasource.password}") String dbpassword;
    @Value("${app.datasource.url}") String dburl;
    @Value("${app.datasource.driverClassName}") String dbclass;
    @Value("${check.consistency:0}") int checkConsistency;
    @Value("${hibernate.init:0}") int hibernateInit;
    @Value("${hibernate.use:0}") int useHibernate;
    @Value("${sapconnection.file}") String SAPCONNECTION;
    @Value("${websocket.use:0}") int websocketUse;
    @Value("${websocket.port:9001}") int websocketPort;
    String libloaderpath;
    String mavenpath;
    private @Getter @Setter boolean initmessage = false;
    private @Getter @Setter SiteTreeBean sitetree;
    @Value("${server.name:Clownfish Server Open Source}") String servername;
    @Value("${server.x-powered:Clownfish Server Open Source by Rainer Sulzbach}") String serverxpowered;
    
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
    
    public void initClasspath() {
        cfclassLoader = null;
        cfclassCompiler = null;
        initmessage = true;
        init();
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
        
        if (null == authtokenlist) {
            authtokenlist = new AuthTokenList();
        }
        
        if (null == authtokenlistclasscontent) {
            authtokenlistclasscontent = new AuthTokenListClasscontent();
        }
        
        // read all System Properties of the property table
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }

        if (null == staticSiteUtil) {
            staticSiteUtil = new StaticSiteUtil(propertyUtil);
        }

        if (null == cfclassLoader)
        {
            cfclassLoader = new CfClassLoader();
        }
        
        if (null == cfclassCompiler)
        {
            cfclassCompiler = new CfClassCompiler();
            cfclassCompiler.setClownfish(this);
            cfclassCompiler.init(cfclassLoader, propertyUtil, cfjavaService);
        }
        
        libloaderpath = propertyUtil.getPropertyValue("folder_libs");
        
        if (null == beanUtil)
            beanUtil = new BeanUtil();
        if ((!libloaderpath.isBlank()) && (null != libloaderpath)) 
            beanUtil.init(libloaderpath);
        
        mavenpath = propertyUtil.getPropertyValue("folder_maven");
        
        if ((!mavenpath.isBlank()) && (null != mavenpath)) {
            if (null == classpathUtil) {
                classpathUtil = new ClassPathUtil();
                classpathUtil.init(cfclassLoader);
            }
            classpathUtil.addPath(mavenpath);
        }
        
        if (null == mavenlist) {
            mavenlist = new MavenList();
            mavenlist.setClasspathUtil(classpathUtil);
        }
        
        Thread compileThread = new Thread(cfclassCompiler);
        compileThread.start();
        
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
            System.out.println("INIT CLOWNFISH CMS");
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
            System.out.println("                            *@@@@@@@@%          [" + clownfishutil.getVersion() + "] on Tomcat " + clownfishutil.getVersionTomcat()+ " with Mojarra " + clownfishutil.getVersionMojarra());
            System.out.println(ansi().reset());
            
            // Check Consistence
            if (checkConsistency > 0) {
                consistencyUtil.checkConsistency();
            }
            
            // Generate Hibernate DOM Mapping
            if (null == hibernateInitializer) {
                hibernateInitializer = new HibernateInit(servicestatus, cfclassService, cfattributService, cfclasscontentService, cfattributcontentService, cflistcontentService, cfclasscontentkeywordService, cfkeywordService, dburl);
                hibernateUtil.init(hibernateInitializer);
                hibernateUtil.setHibernateInit(hibernateInit);
                
                Thread hibernateThread = new Thread(hibernateUtil);
                hibernateThread.start();
            }
            
            propertylist.setClownfish(this);
            if (null == defaultUtil) {
                defaultUtil = new DefaultUtil();
            }

            if (null == mailUtil)
                mailUtil = new MailUtil(propertyUtil);

            if (null == pdfUtil)
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
                    rfcfunctioninterface = new RFC_GET_FUNCTION_INTERFACE(sapc);
                    rfcgroupsearch = new RFC_GROUP_SEARCH(sapc);
                    rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapc);
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
                markdownUtil = new MarkdownUtil(propertylist);
                markdownUtil.initOptions();
            } else {
                markdownUtil.initOptions();
            }
            if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getIndex_folder().isEmpty())) {
                // Call a parallel thread to index the content in Lucene
                if (null == contentIndexer) {
                    contentIndexer = new ContentIndexer(cfattributcontentService, indexService);
                }
                Thread contentindexer_thread = new Thread(contentIndexer);
                contentindexer_thread.start();
                LOGGER.info("CONTENTINDEXER RUN");
                if (null == assetIndexer) {
                    assetIndexer = new AssetIndexer(cfassetService, indexService, propertylist);
                }
                Thread assetindexer_thread = new Thread(assetIndexer);
                assetindexer_thread.start();
                LOGGER.info("ASSETINDEXER RUN");
                
                if (null == databasetableIndexer) {
                    databasetableIndexer = new DatabasetableIndexer(cfsearchdatabaseService, cfdatasourceService, databaseUtil, indexService);
                }
                Thread databasetableIndexer_thread = new Thread(databasetableIndexer);
                databasetableIndexer_thread.start();
                LOGGER.info("DATABASETABLEINDEXER RUN");
                
                indexService.getWriter().commit();
            }
           
            // Init Site Metadata Map
            if (null == metainfomap) {
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
        } catch (IOException | SchedulerException ex) {
            LOGGER.error(ex.getMessage());
        }
        folderUtil.init();
        servicestatus.setMessage("Clownfish is online");
        servicestatus.setOnline(true);
        LOGGER.info("INIT CLOWNFISH END");
        
        if (1 == websocketUse) {
            if (null == wss) {
                wss = new WebSocketServer(this);
                wss.setPort(websocketPort);
                try {
                    Thread websocketserver_thread = new Thread(wss);
                    websocketserver_thread.start();
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public Clownfish() {
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
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            String token = request.getHeader("cf_token");
            String login_token = request.getHeader("cf_login_token");
            boolean alias = false;
            String aliasname = "";
            try {
                ArrayList urlParams = new ArrayList();
                // fetch site by name or aliasname
                CfSite cfsite = null;
                try {
                    cfsite = cfsiteService.findByName(name);
                } catch (Exception ex) {
                    try {
                        aliasname = name;
                        cfsite = cfsiteService.findByAliaspath(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception e1) {
                        try {
                            aliasname = name;
                            cfsite = cfsiteService.findByShorturl(name);
                            name = cfsite.getName();
                            alias = true;
                        } catch (Exception ey) {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                
                if (!cfsite.isSearchresult()) {
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    if (alias) {
                        path = path.replaceFirst(aliasname, name);
                    } else {
                        path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    }

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
                        if (path.startsWith("/")) {
                            name = path.substring(1);
                        } else {
                            name = path;
                        }
                        if (name.lastIndexOf("/")+1 == name.length()) {
                            name = name.substring(0, name.length()-1);
                        }
                    }
                } else {
                    searchmetadata.clear();
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    String query = "";
                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                query += params[i];
                            }
                        }
                    }
                    if (query.isEmpty()) {
                        Map<String, String[]> parammap = request.getParameterMap();
                        query = parammap.get("query")[0];
                    }

                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

                    searcher.setIndexPath(folderUtil.getIndex_folder());
                    long startTime = System.currentTimeMillis();
                    SearchResult searchresult = searcher.search(query, searchlimit);
                    long endTime = System.currentTimeMillis();

                    LOGGER.info("Search Time :" + (endTime - startTime));
                    searchmetadata.clear();
                    searchmetadata.put("cfSearchQuery", query);
                    searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
                    searchcontentmap.clear();
                    if (null != searchresult) {
                        if (null != searchresult.getFoundSites()) {
                            searchresult.getFoundSites().stream().forEach((site) -> {
                                if (null != site) {
                                    searchcontentmap.put(site.getName(), site);
                                }
                            });
                        }
                        searchassetmap.clear();
                        if (null != searchresult.getFoundAssets()) {
                            searchresult.getFoundAssets().stream().forEach((asset) -> {
                                if (null != asset) {
                                    searchassetmap.put(asset.getName(), asset);
                                }
                            });
                        }
                        searchassetmetadatamap.clear();
                        if (null != searchresult.getFoundAssetsMetadata()) {
                            searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                                searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
                            });
                        }
                        searchclasscontentmap.clear();
                        if (null != searchresult.getFoundClasscontent()) {
                            searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                                searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
                            });
                        }
                    }
                }

                userSession = request.getSession();
                Map<String, String[]> querymap = request.getParameterMap();

                ArrayList queryParams = new ArrayList();
                if ((null != token) && (!token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_token");
                    jfp.setValue(token);
                    queryParams.add(jfp);
                }
                if ((null != login_token) && (!login_token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_login_token");
                    jfp.setValue(login_token);
                    queryParams.add(jfp);
                }
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
                Future<ClownfishResponse> cfResponse = makeResponse(name, queryParams, urlParams, false, null, clientinfo);
                if (cfResponse.get().getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                } else {
                    switch (cfResponse.get().getErrorcode()) {
                        case 1:
                        case 2:
                        case 4:
                            response.setContentType("text/html");
                            response.setCharacterEncoding("UTF-8");
                            break;
                        case 3:
                        case 5:
                            response.sendRedirect("/" + cfResponse.get().getRelocation());
                            break;
                    }
                }
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.get().getOutput().getBytes(this.characterencoding));
            } catch (IOException | InterruptedException | ExecutionException | ParseException ex) {
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
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/upload/{name}")
    public void universalPostMultipart(@PathVariable("name") String name, @Context MultipartRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            
            userSession = request.getSession();
            if (request.getContentType().startsWith("multipart/form-data")) {
                Map<String, String[]> parameterMap = request.getParameterMap();
                List<FileItem> fis = request.getFileItems("file");
                List<JsonFormParameter> map = new ArrayList<>();
                for (String key : parameterMap.keySet()) {
                    map.add(new JsonFormParameter(key, parameterMap.get(key)[0]));
                }
                
                addHeader(response, clownfishutil.getVersion());
                Future<ClownfishResponse> cfResponse = makeResponse(name, map, urlParams, false, fis, clientinfo);
                if (cfResponse.get().getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    
                    if ((null != uploadbean) && (null != uploadbean.getUploadpath()) && (!uploadbean.getUploadpath().isEmpty())) {
                        for (FileItem fi : fis) {
                            if ((uploadbean.getFileitemmap().get(fi.getName())) || (uploadbean.getFileitemmap().isEmpty())) {
                                File result = new File(uploadbean.getUploadpath() + File.separator + fi.getName());
                                boolean fileexists = result.exists();
                                InputStream inputStream = fi.getInputStream();
                                if (!fileexists) {
                                    try (FileOutputStream fileOutputStream = new FileOutputStream(result)) {
                                        byte[] buffer = new byte[64535];
                                        int bulk;
                                        while (true) {
                                            bulk = inputStream.read(buffer);
                                            if (bulk < 0) {
                                                break;
                                            }
                                            fileOutputStream.write(buffer, 0, bulk);
                                            fileOutputStream.flush();
                                        }
                                        fileOutputStream.close();
                                    }
                                }
                                inputStream.close();
                            }
                        }
                    }
                    
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.get().getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.get().getOutput().getBytes(this.characterencoding)); 
                }
            } else {
                String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                Gson gson = new Gson();
                List<JsonFormParameter> map;
                map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
                
                // fetch site by name or aliasname
                CfSite cfsite = null;
                try {
                    cfsite = cfsiteService.findByName(name);
                } catch (Exception ex) {
                    try {
                        cfsite = cfsiteService.findByAliaspath(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception e1) {
                        try {
                            cfsite = cfsiteService.findByShorturl(name);
                            name = cfsite.getName();
                            alias = true;
                        } catch (Exception ey) {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                if (cfsite.isSearchresult()) {
                    if (searchcontentmap.isEmpty()) {
                        String query = "";
                        for (JsonFormParameter jsp : map) {
                            if (0 == jsp.getName().compareToIgnoreCase("search")) {
                                query += jsp.getValue();
                            }
                        }
                        String[] searchexpressions = query.split(" ");
                        searchUtil.updateSearchhistory(searchexpressions);

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
                    }
                }

                addHeader(response, clownfishutil.getVersion());
                Future<ClownfishResponse> cfResponse = makeResponse(name, map, urlParams, false, null, clientinfo);
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
        } catch (IOException | InterruptedException | ExecutionException | PageNotFoundException | IllegalStateException | ParseException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/{name}/**")
    public void universalPostHttp(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
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
            String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Gson gson = new Gson();
            List<JsonFormParameter> map;
            map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());

            // fetch site by name or aliasname
            CfSite cfsite = null;
            try {
                cfsite = cfsiteService.findByName(name);
            } catch (Exception ex) {
                try {
                    cfsite = cfsiteService.findByAliaspath(name);
                    name = cfsite.getName();
                    alias = true;
                } catch (Exception e1) {
                    try {
                        cfsite = cfsiteService.findByShorturl(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception ey) {
                        throw new PageNotFoundException("PageNotFound Exception: " + name);
                    }
                }
            }
            if (cfsite.isSearchresult()) {
                if (searchcontentmap.isEmpty()) {
                    String query = "";
                    for (JsonFormParameter jsp : map) {
                        if (0 == jsp.getName().compareToIgnoreCase("search")) {
                            query += jsp.getValue();
                        }
                    }
                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

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
                }
            }

            addHeader(response, clownfishutil.getVersion());
            Future<ClownfishResponse> cfResponse = makeResponse(name, map, urlParams, false, null, clientinfo);
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
        } catch (IOException | InterruptedException | ExecutionException | PageNotFoundException | IllegalStateException | ParseException ex) {
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
     * @param fileitems
     * @return 
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException 
     */
    @Async
    public Future<ClownfishResponse> makeResponse(String name, List<JsonFormParameter> postmap, List urlParams, boolean makestatic, List<FileItem> fileitems, ClientInformation clientinfo) throws PageNotFoundException {
        authtokenlist.setPropertyUtil(propertyUtil);
        ClownfishResponse cfresponse = new ClownfishResponse();
        if (null != sitecontentmap) {
            sitecontentmap.clear();
        }
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
            // manage urlParams
            clownfishutil.addUrlParams(parametermap, urlParams);
            
            preview = false;
            if (parametermap.containsKey("preview")) {    // check mode for display (preview or productive)
                if (parametermap.get("preview").toString().compareToIgnoreCase("true") == 0) {
                    preview = true;
                }
            }
            
            if (parametermap.containsKey("modus")) {    // check mode for display (staging or dev)
                if (parametermap.get("modus").toString().compareToIgnoreCase("dev") == 0) {
                    modus = DEVELOPMENT;
                }
            }
            
            cf_job = false;
            if (parametermap.containsKey("cf_job")) {    // check mode for job call
                if (parametermap.get("cf_job").toString().compareToIgnoreCase("true") == 0) {
                    cf_job = true;
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
            
            String login_token = "";
            if (parametermap.containsKey("cf_login_token")) {    // check token for access manager
                login_token = parametermap.get("cf_login_token").toString();
            }
            // !ToDo: #95 check AccessManager
            String token = "";
            if (parametermap.containsKey("cf_token")) {    // check token for access manager
                token = parametermap.get("cf_token").toString();
            }
            if (accessmanager.checkAccess(token, TYPE_SITE.getValue(), BigInteger.valueOf(cfsite.getId()))) {
                if (((cfsite.isOffline()) && ((preview) && (authtokenlist.checkValidToken(login_token)))) || (!cfsite.isOffline())) {
                    // Site has not job flag
                    if ((!cfsite.isJob()) || cf_job) {
                        // increment site hitcounter
                        if ((!preview) && (modus == STAGING)) {
                            long hitcounter = cfsite.getHitcounter().longValue();
                            cfsite.setHitcounter(BigInteger.valueOf(hitcounter+1));
                            cfsiteService.edit(cfsite);
                        }

                        // Site has static flag
                        if ((cfsite.isStaticsite()) && (!makestatic) && (!preview)) {
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

                            if ((authtokenlist.checkValidToken(login_token)) || (isOnline(name, urlParams))) {
                                cfresponse = getStaticSite(name, getUrlParamName(name, urlParams), postmap, urlParams);
                                if (0 == cfresponse.getErrorcode()) {
                                    return new AsyncResult<>(cfresponse);
                                } else {
                                    Future<ClownfishResponse> cfStaticResponse = makeResponse(name, postmap, urlParams, true, fileitems, clientinfo);
                                    try {
                                        if (urlParams.isEmpty()) {
                                            String aliasname = cfsite.getAliaspath();
                                            staticSiteUtil.generateStaticSite(name, aliasname, cfStaticResponse.get().getOutput(), cfassetService, folderUtil);
                                        }
                                        //return makeResponse(name, postmap, urlParams, false);
                                        return cfStaticResponse;
                                    } catch (InterruptedException | ExecutionException ex) {
                                        LOGGER.error(ex.getMessage());
                                        return makeResponse(name, postmap, urlParams, false, fileitems, clientinfo);
                                    }
                                }
                            } else {
                                cfresponse.setErrorcode(4);
                                cfresponse.setOutput("Offline");
                                return new AsyncResult<>(cfresponse);
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
                                if (null != cfsite.getTemplateref()) {
                                    CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().getId());
                                    // fetch the dependend template
                                    boolean isScripted = false;
                                    switch (cftemplate.getScriptlanguage()) {
                                        case 0:                                     // FREEMARKER
                                            fmRoot = new LinkedHashMap();
                                            freemarkerTemplateloader.setModus(modus);

                                            freemarkerCfg = new freemarker.template.Configuration();
                                            freemarkerCfg.setDefaultEncoding("UTF-8");
                                            freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                                            freemarkerCfg.setLocalizedLookup(false);
                                            freemarkerCfg.setLocale(Locale.GERMANY);
                                            freemarkerCfg.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);

                                            fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                                            isScripted = true;
                                            break;
                                        case 1:                                     // VELOCITY
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
                                            velTemplate.setData(runtimeServices.parse(reader, velTemplate));
                                            velTemplate.initDocument();
                                            isScripted = true;
                                            break;
                                        default:
                                            break;
                                    }

                                    long currentTemplateVersion;
                                    try {
                                        currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                                    } catch (NullPointerException ex) {
                                        currentTemplateVersion = 0;
                                    }

                                    String gzip = propertyUtil.getPropertySwitch("html_gzip", cfsite.getGzip());
                                    if (gzip.compareToIgnoreCase("on") == 0) {
                                        gzipswitch.setGzipon(true);
                                    }
                                    String htmlcompression = propertyUtil.getPropertySwitch("html_compression", cfsite.getHtmlcompression());
                                    HtmlCompressor htmlcompressor = new HtmlCompressor();
                                    htmlcompressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
                                    htmlcompressor.setPreserveLineBreaks(false);
                                    Writer out = new StringWriter();

                                    // fetch the dependend stylesheet, if available
                                    String cfstylesheet = "";
                                    if (cfsite.getStylesheetref() != null) {
                                        cfstylesheet = ((CfStylesheet) cfstylesheetService.findById(cfsite.getStylesheetref().getId())).getContent();
                                        if (htmlcompression.compareToIgnoreCase("on") == 0) {
                                            htmlcompressor.setCompressCss(true);
                                            cfstylesheet = htmlcompressor.compress(cfstylesheet);
                                        }
                                    }

                                    // fetch the dependend javascript, if available
                                    String cfjavascript = "";
                                    if (cfsite.getJavascriptref() != null) {
                                        cfjavascript = ((CfJavascript) cfjavascriptService.findById(cfsite.getJavascriptref().getId())).getContent();
                                        if (htmlcompression.compareToIgnoreCase("on") == 0) {
                                            htmlcompressor.setCompressJavaScript(true);
                                            cfjavascript = htmlcompressor.compress(cfjavascript);
                                        }
                                    }

                                    if (1 != cftemplate.getType()) {                                                                        // NORMAL or Preview Template
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
                                    } else {                                                                                            // LAYOUT Template
                                        // Fetch the dependent content
                                        List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiteref(cfsite.getId());

                                        List<CfLayoutcontent> contentlist = layoutcontentlist.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("C") == 0).collect(Collectors.toList());
                                        List<CfClasscontent> classcontentlist = new ArrayList<>();
                                        for (CfLayoutcontent layoutcontent : contentlist) {
                                            if ((preview) && (authtokenlist.checkValidToken(login_token))) {      // ToDo check accessmanager
                                                if (layoutcontent.getPreview_contentref().longValue() > 0) {
                                                    classcontentlist.add(cfclasscontentService.findById(layoutcontent.getPreview_contentref().longValue()));
                                                }
                                            } else {
                                                if ((null != layoutcontent.getContentref()) && (layoutcontent.getContentref().longValue() > 0)) {
                                                    classcontentlist.add(cfclasscontentService.findById(layoutcontent.getContentref().longValue()));
                                                }
                                            }
                                        }
                                        sitecontentmap = siteutil.getClasscontentmapList(classcontentlist);

                                        // fetch the dependend datalists, if available
                                        contentlist = layoutcontentlist.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("DL") == 0).collect(Collectors.toList());
                                        List<CfList> sitelist = new ArrayList<>();
                                        for (CfLayoutcontent layoutcontent : contentlist) {
                                            if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                                                if (layoutcontent.getPreview_contentref().longValue() > 0) {
                                                    sitelist.add(cflistService.findById(layoutcontent.getPreview_contentref().longValue()));
                                                }
                                            } else {
                                                if ((null != layoutcontent.getContentref()) && (layoutcontent.getContentref().longValue() > 0)) {
                                                    sitelist.add(cflistService.findById(layoutcontent.getContentref().longValue()));
                                                }
                                            }
                                        }
                                        sitecontentmap = siteutil.getSitelist_list(sitelist, sitecontentmap);

                                        // fetch the dependend assetlibraries, if available
                                        contentlist = layoutcontentlist.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("AL") == 0).collect(Collectors.toList());
                                        List<CfAssetlist> assetlibrary_list = new ArrayList<>();
                                        for (CfLayoutcontent layoutcontent : contentlist) {
                                            if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                                                if (layoutcontent.getPreview_contentref().longValue() > 0) {
                                                    assetlibrary_list.add(cfassetlistService.findById(layoutcontent.getPreview_contentref().longValue()));
                                                }
                                            } else {
                                                if ((null != layoutcontent.getContentref()) && (layoutcontent.getContentref().longValue() > 0)) {
                                                    assetlibrary_list.add(cfassetlistService.findById(layoutcontent.getContentref().longValue()));
                                                }
                                            }
                                        }
                                        sitecontentmap = siteutil.getAssetlibrary(assetlibrary_list, sitecontentmap);

                                        // fetch the site keywordlibraries
                                        contentlist = layoutcontentlist.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("KL") == 0).collect(Collectors.toList());
                                        List<CfKeywordlist> keywordlibrary_list = new ArrayList<>();
                                        for (CfLayoutcontent layoutcontent : contentlist) {
                                            if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                                                if (layoutcontent.getPreview_contentref().longValue() > 0) {
                                                    keywordlibrary_list.add(cfkeywordlistService.findById(layoutcontent.getPreview_contentref().longValue()));
                                                }
                                            } else {
                                                if ((null != layoutcontent.getContentref()) && (layoutcontent.getContentref().longValue() > 0)) {
                                                    keywordlibrary_list.add(cfkeywordlistService.findById(layoutcontent.getContentref().longValue()));
                                                }
                                            }
                                        }
                                        sitecontentmap = siteutil.getSiteKeywordlibrary(keywordlibrary_list, sitecontentmap);
                                    }

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
                                    metainfomap.put("templateversion", String.valueOf(currentTemplateVersion));
                                    if (null != clientinfo) {
                                        if (null != clientinfo.getHostname()) metainfomap.put("hostname", clientinfo.getHostname());
                                        if (null != clientinfo.getIpadress()) metainfomap.put("ipaddress", clientinfo.getIpadress());
                                    }

                                    // instantiate Template Beans
                                    networkbean = new NetworkTemplateBean();
                                    webservicebean = new WebServiceTemplateBean();
                                    websocketbean = new WebSocketTemplateBean();
                                    websocketbean.setWebsocketPort(websocketPort);
                                    uploadbean = new UploadTemplateBean();
                                    if (null != fileitems) {
                                        uploadbean.setFileitemlist(fileitems);
                                    }

                                    emailbean = new EmailTemplateBean();
                                    emailbean.init(propertyUtil.getPropertymap(), mailUtil, propertyUtil);
                                    // send a mail, if email properties are set
                                    if (emailproperties != null) {
                                        try {
                                            sendRespondMail(emailproperties.getSendto(), emailproperties.getSubject(), emailproperties.getBody());
                                        } catch (Exception ex) {
                                            LOGGER.error(ex.getMessage());
                                        }
                                    }

                                    if (sapSupport) {
                                        List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                                        sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                                        sapbean = new SAPTemplateBean();
                                        sapbean.init(sapc, sitesaprfclist, rpytableread, postmap);
                                    }

                                    databasebean = new DatabaseTemplateBean(propertyUtil);
                                    importbean = new ImportTemplateBean();
                                    pdfbean = new PDFTemplateBean();
                                    pdfbean.init(pdfUtil);
                                    if (!sitedatasourcelist.isEmpty()) {
                                        databasebean.init(sitedatasourcelist, cfdatasourceService);
                                        importbean.init(sitedatasourcelist, cfdatasourceService);
                                    }
                                    externalclassproviderbean = new ExternalClassProvider(cfclassCompiler);
                                    contentUtil.init(markdownUtil, name, urlParams);
                                    contentbean = new ContentTemplateBean(propertyUtil, contentUtil);
                                    contentbean.init(cfclasscontentService, cfattributcontentService, cflistService, cflistcontentService, cfclassService, cfassetlistService, cfassetlistcontentService, cfassetService, useHibernate);

                                    if (isScripted) {                                                                           // NORMAL Template
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
                                                    fmRoot.put("websocketBean", websocketbean);
                                                    fmRoot.put("uploadBean", uploadbean);
                                                    fmRoot.put("pdfBean", pdfbean);
                                                    fmRoot.put("classBean", externalclassproviderbean);
                                                    fmRoot.put("contentBean", contentbean);

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

                                                    for (Class<?> tpbc : beanUtil.getLoadabletemplatebeans()) {
                                                        Constructor<?> ctor;
                                                        try {
                                                            ctor = tpbc.getConstructor();
                                                            Object object = ctor.newInstance();
                                                            fmRoot.put(tpbc.getName().replaceAll("\\.", "_"), object);
                                                        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    }

                                                    /*
                                                    for (Class<?> c : classpathUtil.getClass_set()) {
                                                        Constructor<?> ctor;
                                                        try {
                                                            if (!Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()))
                                                            {
                                                                ctor = c.getConstructor();
                                                                Object object = ctor.newInstance();
                                                                fmRoot.put(c.getName().replaceAll("\\.", "_"), object);
                                                            }
                                                        } catch (NoClassDefFoundError | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                            if (ex instanceof NoSuchMethodException || ex instanceof IllegalAccessException || ex instanceof InvocationTargetException || ex instanceof NoClassDefFoundError)
                                                                continue;

                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    }
                                                    */

                                                    Map finalFmRoot = fmRoot;
                                                    cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                                                    {
                                                        Constructor<?> ctor;
                                                        try
                                                        {
                                                            ctor = k.getConstructor();
                                                            Object object = ctor.newInstance();
                                                            finalFmRoot.put(k.getName().replaceAll("\\.", "_"), object);
                                                        }
                                                        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                                                        {
                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    });

                                                    try {
                                                        if (null != fmTemplate) {
                                                            if (1 == cftemplate.getType()) {
                                                                try {
                                                                    String output = manageLayout(cfsite, cftemplate.getName(), cftemplate.getContent(), cfstylesheet, cfjavascript, parametermap);
                                                                    output = interpretscript(output, cftemplate, cfstylesheet, cfjavascript, parametermap);
                                                                    out.write(output);
                                                                } catch (ClownfishTemplateException ex) {
                                                                    //LOGGER.error(ex.getMessage());
                                                                    cfresponse.setErrorcode(5);
                                                                    cfresponse.setOutput("ClownfishTemplateException");
                                                                    cfresponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                                                                    return new AsyncResult<>(cfresponse);
                                                                }
                                                            } else {
                                                                freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                                                                env.process();
                                                            }
                                                        }
                                                    } catch (freemarker.template.TemplateException ex) {
                                                        System.out.println(name);
                                                        postmap.stream().forEach((jfp) -> {
                                                            System.out.println(jfp.getName() + " -> " + jfp.getValue());
                                                        });
                                                        urlParams.stream().forEach((jfp) -> {
                                                            System.out.println(jfp.toString());
                                                        });
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
                                                    velContext.put("websocketBean", websocketbean);
                                                    velContext.put("uploadBean", uploadbean);
                                                    velContext.put("pdfBean", pdfbean);
                                                    velContext.put("classBean", externalclassproviderbean);
                                                    velContext.put("contentBean", contentbean);

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

                                                    for (Class tpbc : beanUtil.getLoadabletemplatebeans()) {
                                                        Constructor<?> ctor;
                                                        try {
                                                            ctor = tpbc.getConstructor();
                                                            Object object = ctor.newInstance(new Object[] { });
                                                            velContext.put(tpbc.getName().replaceAll("\\.", "_"), object);
                                                        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    }

                                                    /*
                                                    for (Class<?> c : classpathUtil.getClass_set()) {
                                                        Constructor<?> ctor;
                                                        try {
                                                            if (!Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()))
                                                            {
                                                                ctor = c.getConstructor();
                                                                Object object = ctor.newInstance();
                                                                velContext.put(c.getName().replaceAll("\\.", "_"), object);
                                                            }
                                                        } catch (NoClassDefFoundError | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                            if (ex instanceof NoSuchMethodException || ex instanceof IllegalAccessException || ex instanceof InvocationTargetException || ex instanceof NoClassDefFoundError)
                                                                continue;

                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    }
                                                    */

                                                    org.apache.velocity.VelocityContext finalvelContext = velContext;
                                                    cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                                                    {
                                                        Constructor<?> ctor;
                                                        try
                                                        {
                                                            ctor = k.getConstructor();
                                                            Object object = ctor.newInstance();
                                                            finalvelContext.put(k.getName().replaceAll("\\.", "_"), object);
                                                        }
                                                        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                                                        {
                                                            LOGGER.error(ex.getMessage());
                                                        }
                                                    });

                                                    if (null != velTemplate) {
                                                        if (1 == cftemplate.getType()) {
                                                            try {
                                                                String output = manageLayout(cfsite, cftemplate.getName(), cftemplate.getContent(), cfstylesheet, cfjavascript, parametermap);
                                                                output = interpretscript(output, cftemplate, cfstylesheet, cfjavascript, parametermap);
                                                                out.write(output);
                                                            } catch (TemplateException | ClownfishTemplateException ex) {
                                                                //LOGGER.error(ex.getMessage());
                                                                cfresponse.setErrorcode(5);
                                                                cfresponse.setOutput("ClownfishTemplateException");
                                                                cfresponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                                                                return new AsyncResult<>(cfresponse);
                                                            }
                                                        } else {
                                                            velTemplate.merge(velContext, out);
                                                        }
                                                    }

                                                }
                                                break;
                                            default:                                            // HTML
                                        }
                                    } else {                                                                                // LAYOUT Template
                                        if (1 == cftemplate.getType()) {
                                            String output = "";
                                            try {
                                                output = manageLayout(cfsite, cftemplate.getName(), cftemplate.getContent(), cfstylesheet, cfjavascript, parametermap);
                                            } catch (ClownfishTemplateException ex) {
                                                //LOGGER.error(ex.getMessage());
                                                cfresponse.setErrorcode(5);
                                                cfresponse.setOutput("ClownfishTemplateException");
                                                cfresponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                                                return new AsyncResult<>(cfresponse);
                                            }
                                            out.write(output);
                                            out.flush();
                                            out.close();
                                        } else {
                                            out.write(cftemplate.getContent());
                                            out.flush();
                                            out.close();
                                        }
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
                                } else {
                                    cfresponse.setErrorcode(4);
                                    cfresponse.setOutput("Template not set");
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
                }   else {
                    cfresponse.setErrorcode(4);
                    cfresponse.setOutput("Offline");
                    cfresponse.setRelocation(cfsite.getLoginsite());
                    return new AsyncResult<>(cfresponse);
                }
            } else {
                cfresponse.setErrorcode(3);
                cfresponse.setOutput("No access");
                cfresponse.setRelocation(cfsite.getLoginsite());
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
        if (null != userSession) {
            Collections.list(userSession.getAttributeNames()).stream().filter((key) -> (key.startsWith("session"))).forEach((key) -> {
                String attributevalue = (String) userSession.getAttribute(key);
                parametermap.put(key, attributevalue);
            });
        }
    }

    /**
     * addHeader
     * 
     */
    private void addHeader(HttpServletResponse response, String version) {
        String serverString = servername.replaceAll("#version#", version);
        String serverxpowerdedString = serverxpowered.replaceAll("#version#", version);
        response.addHeader("Server", serverString);
        response.addHeader("X-Powered-By", serverxpowerdedString);
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
    private ClownfishResponse getStaticSite(String sitename, String siteurlname, List<JsonFormParameter> postmap, List urlParams) {
        ClownfishResponse cfResponse = new ClownfishResponse();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(folderUtil.getStatic_folder() + File.separator + siteurlname), "UTF-8"));
            StringBuilder sb = new StringBuilder(1024);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            cfResponse.setOutput(sb.toString());
            cfResponse.setErrorcode(0);
            br.close();
            return cfResponse;
        } catch (IOException ex) {
            CfSite cfsite = cfsiteService.findByName(sitename);
            String aliasname = cfsite.getAliaspath();
            Future<ClownfishResponse> cfStaticResponse;
            try {
                cfStaticResponse = makeResponse(sitename, postmap, urlParams, true, null, null);
                if (0 == cfStaticResponse.get().getErrorcode()) {
                    if (!urlParams.isEmpty()) {
                        staticSiteUtil.generateStaticSite(siteurlname, "", cfStaticResponse.get().getOutput(), cfassetService, folderUtil);
                    } else {
                        staticSiteUtil.generateStaticSite(siteurlname, aliasname, cfStaticResponse.get().getOutput(), cfassetService, folderUtil);
                    }
                } else {
                    deleteStaticSite(sitename, urlParams);
                }
            } catch (PageNotFoundException | InterruptedException | ExecutionException ex1) {
                LOGGER.error(ex.getMessage());
            }
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
    
    private String interpretscript(String templatecontent, CfTemplate cftemplate, String cfstylesheet, String cfjavascript, Map parametermap) throws TemplateException {
        StringWriter out = new StringWriter();
        try {
            freemarker.template.Template fmTemplate = null;
            Map fmRoot = null;
            
            org.apache.velocity.VelocityContext velContext = null;
            org.apache.velocity.Template velTemplate = null;
            
            switch (cftemplate.getScriptlanguage()) {
                case 0:
                    freemarkerStringTemplateloader.setContent(templatecontent);
                    freemarkerStringTemplateloader.putTemplate(cftemplate.getName(), templatecontent);
                    fmRoot = new LinkedHashMap();
                    freemarkerStringTemplateloader.setModus(modus);
                    
                    freemarkerCfg = new freemarker.template.Configuration();
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(freemarkerStringTemplateloader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    freemarkerCfg.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);
                    
                    fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                    break;
                case 1:
                    velContext = new org.apache.velocity.VelocityContext();
                    
                    velTemplate = new org.apache.velocity.Template();
                    org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                    templatecontent = templateUtil.fetchIncludes(templatecontent, modus);
                    StringReader reader = new StringReader(templatecontent);
                    velTemplate.setRuntimeServices(runtimeServices);
                    velTemplate.setData(runtimeServices.parse(reader, velTemplate));
                    velTemplate.initDocument();
                    break;
                default:
                    break;
            }
            
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
                        fmRoot.put("contentBean", contentbean);
                        fmRoot.put("classBean", externalclassproviderbean);
                        
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
                        
                        for (Class<?> tpbc : beanUtil.getLoadabletemplatebeans()) {
                            Constructor<?> ctor;
                            try {
                                ctor = tpbc.getConstructor();
                                Object object = ctor.newInstance();
                                fmRoot.put(tpbc.getName().replaceAll("\\.", "_"), object);
                            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                        
                        /*
                        for (Class<?> c : classpathUtil.getClass_set()) {
                        Constructor<?> ctor;
                        try {
                        if (!Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()))
                        {
                        ctor = c.getConstructor();
                        Object object = ctor.newInstance();
                        fmRoot.put(c.getName().replaceAll("\\.", "_"), object);
                        }
                        } catch (NoClassDefFoundError | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        if (ex instanceof NoSuchMethodException || ex instanceof IllegalAccessException || ex instanceof InvocationTargetException || ex instanceof NoClassDefFoundError)
                        continue;
                        
                        LOGGER.error(ex.getMessage());
                        }
                        }
                        */
                        
                        Map finalFmRoot = fmRoot;
                        cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                        {
                            Constructor<?> ctor;
                            try
                            {
                                ctor = k.getConstructor();
                                Object object = ctor.newInstance();
                                finalFmRoot.put(k.getName().replaceAll("\\.", "_"), object);
                            }
                            catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                            {
                                LOGGER.error(ex.getMessage());
                            }
                        });
                        
                        freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                        try {
                            if (null != fmTemplate) {
                                env.process();
                            }
                        } catch (freemarker.template.TemplateException ex) {
                            //LOGGER.error(ex.getMessage());
                            System.out.println(templatecontent);
                            System.out.println(cftemplate.getName());
                            System.out.println(cfstylesheet);
                            System.out.println(cfjavascript);
                            System.out.println(parametermap.toString());
                            throw new TemplateException(ex.getMessage(), env);
                        } catch (IOException ex) {
                            System.out.println(templatecontent);
                            System.out.println(cftemplate.getName());
                            System.out.println(cfstylesheet);
                            System.out.println(cfjavascript);
                            System.out.println(parametermap.toString());
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
                        velContext.put("contentBean", contentbean);
                        velContext.put("pdfBean", pdfbean);
                        velContext.put("classBean", externalclassproviderbean);
                        
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
                        
                        for (Class tpbc : beanUtil.getLoadabletemplatebeans()) {
                            Constructor<?> ctor;
                            try {
                                ctor = tpbc.getConstructor();
                                Object object = ctor.newInstance(new Object[] { });
                                velContext.put(tpbc.getName().replaceAll("\\.", "_"), object);
                            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                        
                        /*
                        for (Class<?> c : classpathUtil.getClass_set()) {
                        Constructor<?> ctor;
                        try {
                        if (!Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()))
                        {
                        ctor = c.getConstructor();
                        Object object = ctor.newInstance();
                        velContext.put(c.getName().replaceAll("\\.", "_"), object);
                        }
                        } catch (NoClassDefFoundError | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        if (ex instanceof NoSuchMethodException || ex instanceof IllegalAccessException || ex instanceof InvocationTargetException || ex instanceof NoClassDefFoundError)
                        continue;
                        
                        LOGGER.error(ex.getMessage());
                        }
                        }
                        */
                        
                        org.apache.velocity.VelocityContext finalvelContext = velContext;
                        cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                        {
                            Constructor<?> ctor;
                            try
                            {
                                ctor = k.getConstructor();
                                Object object = ctor.newInstance();
                                finalvelContext.put(k.getName().replaceAll("\\.", "_"), object);
                            }
                            catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                            {
                                LOGGER.error(ex.getMessage());
                            }
                        });
                        
                        if (null != velTemplate) {
                            velTemplate.merge(velContext, out);
                        }
                    }
                    break;
                default:                                            // HTML
                    out.append(templatecontent);
            }
            return out.toString();
        } catch (MalformedTemplateNameException ex) {
            LOGGER.error(ex.getMessage());
            return ex.getMessage();
        } catch (freemarker.core.ParseException | org.apache.velocity.runtime.parser.ParseException ex) {
            LOGGER.error(ex.getMessage());
            return ex.getMessage();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return ex.getMessage();
        }
    }
    
    private String manageLayout(CfSite cfsite, String templatename, String templatecontent, String cfstylesheet, String cfjavascript, Map parametermap) throws ClownfishTemplateException {
        String login_token = "";
        if (parametermap.containsKey("cf_login_token")) {    // check token for access manager
            login_token = parametermap.get("cf_login_token").toString();
        }
        
        CfLayout cflayout = new CfLayout(templatename);
        CfTemplate maintemplate = cftemplateService.findByName(templatename);
        templateUtil.fetchLayout(maintemplate);
        Document doc = Jsoup.parse(templatecontent);
        Elements divs = doc.getElementsByAttribute("template");
        for (Element div : divs) {
            String template = div.attr("template");
            String contents = div.attr("contents");
            String datalists = div.attr("datalists");
            String assets = div.attr("assets");
            String assetlists = div.attr("assetlists");
            String keywordlists = div.attr("keywordlists");

            CfDiv cfdiv = new CfDiv();
            cfdiv.setId(div.attr("id"));
            cfdiv.setName(div.attr("template"));
            if (!contents.isEmpty()) {
                cfdiv.getContentArray().addAll(ClownfishUtil.toList(contents.split(",")));
            }
            if (!datalists.isEmpty()) {
                cfdiv.getContentlistArray().addAll(ClownfishUtil.toList(datalists.split(",")));
            }
            if (!assets.isEmpty()) {
                cfdiv.getAssetArray().addAll(ClownfishUtil.toList(assets.split(",")));
            }
            if (!assetlists.isEmpty()) {
                cfdiv.getAssetlistArray().addAll(ClownfishUtil.toList(assetlists.split(",")));
            }
            if (!keywordlists.isEmpty()) {
                cfdiv.getKeywordlistArray().addAll(ClownfishUtil.toList(keywordlists.split(",")));
            }
            if (!template.isEmpty()) {
                CfTemplate cfdivtemplate = cftemplateService.findByName(template);
                List<CfLayoutcontent> layoutcontent = cflayoutcontentService.findBySiterefAndTemplateref(cfsite.getId(), cfdivtemplate.getId());
                long currentTemplateVersion;
                try {
                    currentTemplateVersion = cftemplateversionService.findMaxVersion((cfdivtemplate).getId());
                } catch (NullPointerException ex) {
                    currentTemplateVersion = 0;
                }
                String content = templateUtil.getVersion((cfdivtemplate).getId(), currentTemplateVersion);
                content = templateUtil.fetchIncludes(content, modus);
                content = templateUtil.replacePlaceholders(content, cfdiv, layoutcontent, preview);
                try {
                    content = interpretscript(content, cfdivtemplate, cfstylesheet, cfjavascript, parametermap);
                } catch (TemplateException ex) {
                    //LOGGER.error(ex.getMessage());
                    throw new ClownfishTemplateException(ex.getMessage());
                }
                //System.out.println(out);
                div.removeAttr("template");
                div.removeAttr("contents");
                div.removeAttr("datalists");
                div.removeAttr("assets");
                div.removeAttr("assetlists");
                div.removeAttr("keywordlists");
                
                if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                    div.addClass("cf_div");
                }
                if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                    //templateUtil.fetchLayout(maintemplate);
                    //if ((null != sitetree) && (null != sitetree.getLayout())) {
                        for (CfDiv comp_div : templateUtil.getLayout().getDivs()) {
                            if ((0 == cfdiv.getName().compareToIgnoreCase(comp_div.getName())) && (comp_div.isVisible())) {
                                div.html(content);
                            }
                        }
                    //}
                } else {
                    div.html(content);
                }
            }
            cflayout.getDivArray().put(div.attr("id"), cfdiv);
        }
        if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
            doc.head().append("<script src=\"resources/js/axios.js\"></script>");
            doc.head().append("<link rel=\"stylesheet\" href=\"resources/css/cf_preview.css\">");
            doc.head().append("<link rel=\"stylesheet\" href=\"resources/css/preview_style.css\">");
            doc.head().append("<script src=\"resources/js/cf_preview.js\"></script>");
            doc.head().append("<script src=\"resources/js/preview_script.js\"></script>");
        }
        return doc.html();
    }
    
    private String getUrlParamName(String name, List urlParams) {
        String urlparamname = name;
        if (!urlParams.isEmpty()) {
            for (Object urlparam : urlParams) {
                urlparamname += "_" + (String) (urlparam);
            }
        }
        return urlparamname;
    }
    
    private boolean isOnline(String name, List urlParams) {
        String urlparamname = "";
        if (!urlParams.isEmpty()) {
            for (Object urlparam : urlParams) {
                urlparamname += "/" + (String) (urlparam);
            }
        }
        CfStaticsite staticsite = null;
        if (urlparamname.isEmpty()) {
            try {
                staticsite = cfstaticsiteservice.findBySiteAndUrlparams(name, urlparamname);
            } catch (NoResultException ex) {
                staticsite = new CfStaticsite();
                staticsite.setOffline(false);
                staticsite.setSite(name);
                staticsite.setUrlparams(urlparamname);
                staticsite.setTstamp(new Date());
                cfstaticsiteservice.create(staticsite);
            }
        } else {
            try {
                staticsite = cfstaticsiteservice.findBySiteAndUrlparams(name, urlparamname.substring(1));
            } catch (NoResultException ex) {
                staticsite = new CfStaticsite();
                staticsite.setOffline(false);
                staticsite.setSite(name);
                staticsite.setUrlparams(urlparamname.substring(1));
                staticsite.setTstamp(new Date());
                cfstaticsiteservice.create(staticsite);
            }
        }
        return !staticsite.isOffline();
    }
    
    private void deleteStaticSite(String name, List urlParams) {
        String urlparamname = "";
        if (!urlParams.isEmpty()) {
            for (Object urlparam : urlParams) {
                urlparamname += "/" + (String) (urlparam);
            }
        }
        CfStaticsite staticsite = null;
        if (urlparamname.isEmpty()) {
            try {
                staticsite = cfstaticsiteservice.findBySiteAndUrlparams(name, urlparamname);
                cfstaticsiteservice.delete(staticsite);
            } catch (NoResultException ex) {
            }
        } else {
            try {
                staticsite = cfstaticsiteservice.findBySiteAndUrlparams(name, urlparamname.substring(1));
                cfstaticsiteservice.delete(staticsite);
            } catch (NoResultException ex) {
            }
        }
    }

    private ClientInformation getClientinformation(String ip) {
        ClientInformation ci = new ClientInformation();
        ci.setIpadress(ip);
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ip);
            ci.setHostname(addr.getHostName());
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ci;
    }
}
