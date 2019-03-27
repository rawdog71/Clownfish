package io.clownfish.clownfish;

import KNSAPTools.SAPConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import io.clownfish.clownfish.beans.DatabaseBean;
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.beans.PropertyList;
import static io.clownfish.clownfish.beans.SiteTreeBean.SAPCONNECTION;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.dbentities.CfJavascript;
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
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.sap.SAPUtility;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.DatabaseUtil;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.SiteUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author rawdog71
 */
@RestController
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class Clownfish {
    @Autowired CfSiteService cfsiteService;
    @Autowired CfSitecontentService cfsitecontentService;
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
    @Autowired CfTemplateLoaderImpl freemarkerTemplateloader;
    @Autowired SiteUtil siteutil;
    @Autowired DatabaseUtil databaseUtil;
    
    /*
    @Context
    private UriInfo context;
    */
    @Context
    protected HttpServletResponse response;
    @Context 
    protected HttpServletRequest request;
    
    private GzipSwitch gzipswitch;
    private freemarker.template.Configuration freemarkerCfg;
    private RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private RPY_TABLE_READ rpytableread = null;
    private static SAPConnection sapc = null;
    private boolean sapSupport = false;
    private Map<String, String> propertymap = null;
    private HttpSession userSession;
    private ClownfishConst.ViewModus modus = STAGING;
    private ClownfishUtil clownfishutil;
    private String characterEncoding;
    private String contentType;
    private Locale locale;

    @RequestMapping("/")
    String home() {
        return "Welcome to Clownfish Content Management System";
    }
    
    @GetMapping("/{name}")
    String universalGet(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        userSession = request.getSession();
        this.request = request;
        this.response = response;
        Map<String, String[]> querymap = request.getParameterMap();
        
        ArrayList queryParams = new ArrayList();
        for (Object key : querymap.keySet()) {
            JsonFormParameter jfp = new JsonFormParameter();
            jfp.setName((String) key);
            String[] values = querymap.get(key);
            jfp.setValue(values[0]);
            queryParams.add(jfp);
        }

        return makeResponse(name, queryParams);
    }
    
    @PostMapping("/{name}")
    String universalPost(@PathVariable("name") String name, String content, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        userSession = request.getSession();
        this.request = request;
        this.response = response;
        Map<String, String[]> querymap = request.getParameterMap();
        
        ArrayList queryParams = new ArrayList();
        for (Object key : querymap.keySet()) {
            JsonFormParameter jfp = new JsonFormParameter();
            jfp.setName((String) key);
            String[] values = querymap.get(key);
            jfp.setValue(values[0]);
            queryParams.add(jfp);
        }
        
        return makeResponse(name, queryParams);

        //return makeResponse(name, queryParams);
    }
    
    
    @PostConstruct
    public void init() {
        // Set default values
        modus = STAGING;  // 1 = Staging mode (fetch sourcecode from commited repository) <= default
                    // 0 = Development mode (fetch sourcecode from database)
        characterEncoding = "UTF-8";
        contentType = "text/html";
        locale = new Locale("de");

        // read all System Properties of the property table
        propertymap = propertylist.fillPropertyMap();
        clownfishutil = new ClownfishUtil();
        String sapSupportProp = propertymap.get("sap.support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            //Class<?> clazz = Class.forName("KNSAPTools.SAPConnection");
            //Object sapcinstance = clazz.newInstance();
            sapc = new SAPConnection(SAPCONNECTION, "Clownfish1");
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
            rpytableread = new RPY_TABLE_READ(sapc);
        }
        // Override default values with system properties
        String systemContentType = propertymap.get("response.contenttype");
        String systemCharacterEncoding = propertymap.get("response.characterencoding");
        String systemLocale = propertymap.get("response.locale");
        if (!systemCharacterEncoding.isEmpty()) {
            characterEncoding = systemCharacterEncoding;
        }
        if (!systemContentType.isEmpty()) {
            contentType = systemContentType;
        }
        if (!systemLocale.isEmpty()) {
            locale = new Locale(systemLocale);
        }
        this.gzipswitch = new GzipSwitch();
    }
    
    
    /**
     * Creates a new instance of GenericResource
     */
    public Clownfish() {
    }

    /**
     * Retrieves representation of an instance of de.koenigneurath.freemarkertest.GenericResource
     * @param name
     * @param request
     * @return an instance of java.lang.String
     */
    /*
    @GET
    @Compress
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    public String getHtml(@PathParam("name") String name, @Context HttpServletRequest request) {
        userSession = request.getSession();
        MultivaluedMap querymap = context.getQueryParameters();
        ArrayList queryParams = new ArrayList();
        for (Object key : querymap.keySet()) {
            JsonFormParameter jfp = new JsonFormParameter();
            jfp.setName((String) key);
            List values = (List) querymap.get(key);
            jfp.setValue((String) values.get(0));
            queryParams.add(jfp);
        }
        return makeHTML(name, queryParams);
    }
    */
    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     */
    /*
    @PUT
    @Consumes(MediaType.TEXT_HTML)
    public void putHtml(String content) {
    }
    */
    
    /*
    @POST
    @Compress
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public String postHtml(@PathParam("name") String name, String content, @Context HttpServletRequest request) {
        userSession = request.getSession();
        Gson gson = new Gson(); 
        List<JsonFormParameter> map;
        map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
        
        return makeHTML(name, map);
    }
    */
    
    private String makeResponse(String name, List<JsonFormParameter> postmap) {
        try {
            // Freemarker Template
            freemarker.template.Template fmTemplate = null;
            Map fmRoot = null;
            
            // Velocity Template
            org.apache.velocity.VelocityContext velContext = null;
            org.apache.velocity.Template velTemplate = null;

            // Hole die Parameter Liste
            Map parametermap = clownfishutil.getParametermap(postmap);
            if (parametermap.containsKey("modus")) {    // check mode for display (stageing or dev)
                if (parametermap.get("modus").toString().compareToIgnoreCase("dev") == 0) {
                    modus = DEVELOPMENT;
                }
            }
            
            // Hole die Seite über den Namen
            CfSite cfsite = cfsiteService.findByName(name);
            if ((cfsite.getContenttype() != null)) {
                if (!cfsite.getContenttype().isEmpty()) response.setContentType(cfsite.getContenttype());
            }
            if ((cfsite.getCharacterencoding() != null)) {
                if (!cfsite.getCharacterencoding().isEmpty()) response.setCharacterEncoding(cfsite.getCharacterencoding());
            }
            if ((cfsite.getLocale() != null)) {
                if (!cfsite.getLocale().isEmpty()) response.setLocale(new Locale(cfsite.getLocale()));
            }
            
            try {
                CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().longValue());
                if (0 == cftemplate.getScriptlanguage()) {  // Freemarker Template
                    fmRoot = new LinkedHashMap();
                    freemarkerTemplateloader.setModus(modus);
                    
                    // Hole das zugehörige Template über den name
                    freemarkerCfg = new freemarker.template.Configuration();
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    
                    fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                } else {                                    // Velocity Template
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
                }
                
                // Hole das Stylesheet, falls vorhanden
                String cfstylesheet = "";
                if (cfsite.getStylesheetref() != null) {
                    cfstylesheet = ((CfStylesheet) cfstylesheetService.findById(cfsite.getStylesheetref().longValue())).getContent();
                }

                // Hole das Javascript, falls vorhanden
                String cfjavascript = "";
                if (cfsite.getJavascriptref()!= null) {
                    cfjavascript = ((CfJavascript) cfjavascriptService.findById(cfsite.getJavascriptref().longValue())).getContent();
                }
                
                // Hole sämtlichen Content, der zu dieser Seite referenziert ist
                List<CfSitecontent> sitecontentlist = new ArrayList<>();
                sitecontentlist.addAll(cfsitecontentService.findBySiteref(cfsite.getId()));
                Map sitecontentmap = siteutil.getSitecontentmap(sitecontentlist);
                
                // Hole sämtliche Listen, die zu dieser Seite referenziert sind
                siteutil.getSitelist_list(cfsite, sitecontentmap);

                // Manage Parameters 
                HashMap<String, DatatableProperties> datatableproperties = clownfishutil.getDatatableproperties(postmap);
                EmailProperties emailproperties = clownfishutil.getEmailproperties(postmap);
                HashMap<String, DatatableNewProperties> datatablenewproperties = clownfishutil.getDatatablenewproperties(postmap);
                HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = clownfishutil.getDatatabledeleteproperties(postmap);
                HashMap<String, DatatableUpdateProperties> datatableupdateproperties = clownfishutil.getDatatableupdateproperties(postmap);
                manageSessionVariables(postmap);
                writeSessionVariables(parametermap);
                
                // Hole die Datenquellen zu dieser Seite
                List<CfSitedatasource> sitedatasourcelist = new ArrayList<>();
                sitedatasourcelist.addAll(cfsitedatasourceService.findBySiteref(cfsite.getId()));

                //DatabaseUtil databaseUtil = new DatabaseUtil();
                HashMap<String, HashMap> dbexport = databaseUtil.getDbexport(sitedatasourcelist, datatableproperties, datatablenewproperties, datatabledeleteproperties, datatableupdateproperties);
                sitecontentmap.put("db", dbexport);

                // Hole die SAP RFCs zu dieser Seite
                if (sapSupport) {
                    List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                    sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                    HashMap<String, List> saprfcfunctionparamMap = clownfishutil.getSaprfcfunctionparamMap(sitesaprfclist, rfc_get_function_interface);
                    
                    //Class<?> clazz = Class.forName("KNSAPTools.SAPConnection");
                    //Object sapcinstance = clazz.newInstance();
                    
                    HashMap<String, HashMap> sapexport = new SAPUtility(sapc).getSapExport(sitesaprfclist, saprfcfunctionparamMap, postmap, rpytableread);
                    sitecontentmap.put("sap", sapexport);
                }
                
                // Send Email
                if (emailproperties != null) {
                    try {
                        sendRespondMail(emailproperties.getSendto(), emailproperties.getSubject(), emailproperties.getBody());
                    } catch (Exception ex) {
                        Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Writer out = new StringWriter();
                if (0 == cftemplate.getScriptlanguage()) {  // Freemarker Template
                    DatabaseBean databasebean = new DatabaseBean(sitedatasourcelist, sitecontentmap);
                    fmRoot.put("databaseBean", databasebean);
                    fmRoot.put("css", cfstylesheet);
                    fmRoot.put("js", cfjavascript);
                    fmRoot.put("sitecontent", sitecontentmap); 
                    fmRoot.put("parameter", parametermap);
                    
                    freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                    env.process();
                } else {                                    // Velocity Template
                    DatabaseBean databasebean = new DatabaseBean(sitedatasourcelist, sitecontentmap);
                    velContext.put("databaseBean", databasebean);
                    velContext.put("css", cfstylesheet);
                    velContext.put("js", cfjavascript);
                    velContext.put("sitecontent", sitecontentmap); 
                    velContext.put("parameter", parametermap);
                    
                    velTemplate.merge(velContext, out);
                }
                String gzip;
                gzip = propertymap.get("html.gzip");
                if (gzip == null) {
                     gzip = "off";
                }
                switch (cfsite.getGzip()) {
                    case 1:
                        gzip = "on";
                        break;
                    case 2:
                        gzip = "off";
                        break;    
                }
                if (gzip.compareToIgnoreCase("on") == 0) {
                    gzipswitch.setGzipon(true);
                }
                
                String htmlcompression;
                htmlcompression = propertymap.get("html.compression");
                if (htmlcompression == null) {
                     htmlcompression = "off";
                }
                switch (cfsite.getHtmlcompression()) {
                    case 1:
                        htmlcompression = "on";
                        break;
                    case 2:
                        htmlcompression = "off";
                        break;    
                }
                if (htmlcompression.compareToIgnoreCase("on") == 0) {
                    HtmlCompressor htmlcompressor = new HtmlCompressor();
                    htmlcompressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS);
                    htmlcompressor.setPreserveLineBreaks(false);
                    htmlcompressor.setCompressCss(false);

                    return htmlcompressor.compress(out.toString());
                } else {
                    return out.toString();
                }
            } catch (NoResultException ex) {
                return "No template";
            }     
        } catch (IOException | freemarker.template.TemplateException | org.apache.velocity.runtime.parser.ParseException ex) {
            //Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } 
    }
    
    private void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        MailUtil mailutil = new MailUtil(propertymap.get("mail.smtp.host"), propertymap.get("mail.transport.protocol"), propertymap.get("mail.user"), propertymap.get("mail.password"), propertymap.get("mail.sendfrom"));
        mailutil.sendRespondMail(mailto, subject, mailbody);
    }

    private void manageSessionVariables(List<JsonFormParameter> postmap) {
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                if (jfp.getName().startsWith("session")) {
                    userSession.setAttribute(jfp.getName(), jfp.getValue());
                }
            }
        }
    }
    
    private void writeSessionVariables(Map parametermap) {
        for (String key : Collections.list(userSession.getAttributeNames())) {
            if (key.startsWith("session")) {
                String attributevalue = (String) userSession.getAttribute(key);
                parametermap.put(key, attributevalue);
            }
        }
    }
}
