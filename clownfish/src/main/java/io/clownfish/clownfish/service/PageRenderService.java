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
package io.clownfish.clownfish.service;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import de.destrukt.sapconnection.SAPConnection;
import freemarker.template.TemplateException;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_SITE;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.BeanContext;
import io.clownfish.clownfish.datamodels.CfDiv;
import io.clownfish.clownfish.datamodels.CfLayout;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.JsonFormParameter;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.datamodels.TemplateEngineResult;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfStaticsite;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.exceptions.ClownfishTemplateException;
import io.clownfish.clownfish.exceptions.PageNotFoundException;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.jdbc.DatatableDeleteProperties;
import io.clownfish.clownfish.jdbc.DatatableNewProperties;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.DatatableUpdateProperties;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfStaticsiteService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
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
import io.clownfish.clownfish.utils.AccessManagerUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.DatabaseUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.MarkdownUtil;
import io.clownfish.clownfish.utils.PDFUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.SiteUtil;
import io.clownfish.clownfish.utils.StaticSiteUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author SulzbachR
 */
@Component
@Service
public class PageRenderService {
    @Value("${websocket.port:9001}") int websocketPort;
    @Value("${sapconnection.file}") String SAPCONNECTION;
    @Value("${hibernate.use:0}") int useHibernate;
    
    private ClownfishConst.ViewModus modus = STAGING;
    final transient Logger LOGGER = LoggerFactory.getLogger(PageRenderService.class);
    
    private String contenttype;
    private String characterencoding;
    private String locale;
    private String libloaderpath; 
    private HttpSession userSession;
    private Map<String, String> metainfomap;
    private boolean sapSupport = false;
    private static SAPConnection sapc = null;
    private RPY_TABLE_READ rpytableread = null;
    private RFC_GET_FUNCTION_INTERFACE rfcfunctioninterface = null;
    private RFC_GROUP_SEARCH rfcgroupsearch = null;
    private RFC_FUNCTION_SEARCH rfcfunctionsearch = null;
    private SiteTreeBean sitetree;
    private Map sitecontentmap;
    private RenderContext rendercontext;
    
    private final LayoutService layoutService;
    private final FreemarkerService freemarkerService;
    private final VelocityService velocityService;
    private final AccessManagerUtil accessmanager;
    private final AuthTokenList authtokenlist;
    private final ClownfishUtil clownfishutil;
    private final FolderUtil folderUtil;
    private final TemplateUtil templateUtil;
    private final DatabaseUtil databaseUtil;
    private final ContentUtil contentUtil;
    private final SiteUtil siteutil;
    private final PropertyUtil propertyUtil;
    private final MailUtil mailUtil;
    private final PDFUtil pdfUtil;
    private final MarkdownUtil markdownUtil;
    private final CfClassCompiler cfclassCompiler;
    private final StaticSiteUtil staticSiteUtil;
    
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfAssetService cfassetService;
    @Autowired CfSiteService cfsiteService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    @Autowired CfLayoutcontentService cflayoutcontentService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfListService cflistService;
    @Autowired CfKeywordlistService cfkeywordlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfSitecontentService cfsitecontentService;
    @Autowired CfStaticsiteService cfstaticsiteservice;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfClassService cfclassService;
    
    
    /**
     *
     * @param fs
     * @param vs
     * @param layoutService
     * @param propertyUtil
     * @param mailUtil
     * @param pdfUtil
     * @param markdownUtil
     * @param cfclassCompiler
     * @param staticSiteUtil
     * @param accessmanager
     * @param authtokenlist
     * @param clownfishutil
     * @param folderUtil
     * @param templateUtil
     * @param databaseUtil
     * @param contentUtil
     * @param siteutil
     */
    public PageRenderService(FreemarkerService fs, 
                                VelocityService vs, 
                                LayoutService layoutService, 
                                PropertyUtil propertyUtil, 
                                MailUtil mailUtil, 
                                PDFUtil pdfUtil, 
                                MarkdownUtil markdownUtil, 
                                CfClassCompiler cfclassCompiler, 
                                StaticSiteUtil staticSiteUtil, 
                                AccessManagerUtil accessmanager, 
                                AuthTokenList authtokenlist, 
                                ClownfishUtil clownfishutil, 
                                FolderUtil folderUtil, 
                                TemplateUtil templateUtil, 
                                DatabaseUtil databaseUtil, 
                                ContentUtil contentUtil, 
                                SiteUtil siteutil) {
        this.freemarkerService = fs;
        this.velocityService = vs;
        this.layoutService = layoutService;
        this.propertyUtil = propertyUtil;
        this.mailUtil = mailUtil;
        this.pdfUtil = pdfUtil;
        this.markdownUtil = markdownUtil;
        this.cfclassCompiler = cfclassCompiler;
        this.staticSiteUtil = staticSiteUtil;
        this.accessmanager = accessmanager;
        this.authtokenlist = authtokenlist;
        this.clownfishutil = clownfishutil;
        this.folderUtil = folderUtil;
        this.templateUtil = templateUtil;
        this.databaseUtil = databaseUtil;
        this.contentUtil = contentUtil;
        this.siteutil = siteutil;
    }

    public ClownfishResponse renderPage(RenderContext renderctx) throws PageNotFoundException {
        BeanContext btx = new BeanContext();
        rendercontext = renderctx;
        boolean preview = false;        // is preview
        boolean cf_job = false;         // is job
        GzipSwitch gzipswitch = new GzipSwitch();
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
        
        // Init Site Metadata Map
            if (null == metainfomap) {
                metainfomap = new HashMap();
            }
            metainfomap.put("version", clownfishutil.getVersion());
            metainfomap.put("versionMojarra", clownfishutil.getVersionMojarra());
            metainfomap.put("versionTomcat", clownfishutil.getVersionTomcat());
        
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
            Map parametermap = clownfishutil.getParametermap(renderctx.getPostmap());
            // manage urlParams
            clownfishutil.addUrlParams(parametermap, renderctx.getUrlParams());
            
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
            
            if (parametermap.containsKey("cf_job")) {    // check mode for job call
                if (parametermap.get("cf_job").toString().compareToIgnoreCase("true") == 0) {
                    cf_job = true;
                }
            }
            
            // fetch site by name or aliasname
            CfSite cfsite = null;
            try {
                cfsite = cfsiteService.findByName(renderctx.getName());
            } catch (Exception ex) {
                try {
                    cfsite = cfsiteService.findByAliaspath(renderctx.getName());
                } catch (Exception e1) {
                    throw new PageNotFoundException("PageNotFound Exception: " + renderctx.getName());
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
            //LOGGER.info("TOKEN:" + token);
            if (accessmanager.checkAccess(token, TYPE_SITE.getValue(), BigInteger.valueOf(cfsite.getId()))) {
                //LOGGER.info("LOGIN_TOKEN:" + login_token);
                if (((cfsite.isOffline()) && ((preview) && (authtokenlist.checkValidToken(login_token)))) || (!cfsite.isOffline())) {
                    //LOGGER.info("SHOW SITE:" + cfsite.getName());
                    
                    // Site has not job flag
                    if ((!cfsite.isJob()) || cf_job) {
                        // increment site hitcounter
                        if ((!preview) && (modus == STAGING)) {
                            long hitcounter = cfsite.getHitcounter().longValue();
                            cfsite.setHitcounter(BigInteger.valueOf(hitcounter+1));
                            cfsiteService.edit(cfsite);
                        }

                        // Site has static flag
                        if ((cfsite.isStaticsite()) && (!renderctx.isMakestatic()) && (!preview)) {
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

                            if ((authtokenlist.checkValidToken(login_token)) || (isOnline(renderctx.getName(), renderctx.getUrlParams()))) {
                                cfresponse = getStaticSite(renderctx);
                                if (0 == cfresponse.getErrorcode()) {
                                    return cfresponse;
                                } else {
                                    renderctx.setClientinfo(renderctx.getClientinfo());
                                    renderctx.setFileitems(renderctx.getFileitems());
                                    renderctx.setMakestatic(true);
                                    renderctx.setName(renderctx.getName());
                                    renderctx.setPostmap(renderctx.getPostmap());
                                    renderctx.setReferrer(renderctx.getReferrer());
                                    renderctx.setUrlParams(renderctx.getUrlParams());
                                    
                                    ClownfishResponse cfStaticResponse = renderPage(renderctx);
                                    if (renderctx.getUrlParams().isEmpty()) {
                                        String aliasname = cfsite.getAliaspath();
                                        staticSiteUtil.generateStaticSite(renderctx.getName(), aliasname, cfStaticResponse.getOutput(), cfassetService, folderUtil);
                                    }
                                    return cfStaticResponse;
                                }
                            } else {
                                cfresponse.setErrorcode(4);
                                cfresponse.setOutput("Offline");
                                return cfresponse;
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
                                    //boolean isScripted = false;
                                    
                                    TemplateEngineResult engineResult;
                                    // Das saubere Switch-Statement
                                    switch (cftemplate.getScriptlanguage()) {
                                        case 0 -> engineResult = freemarkerService.init(cftemplate, modus);
                                        case 1 -> engineResult = velocityService.init(cftemplate, modus);
                                        default -> engineResult = new TemplateEngineResult(); // Leeres Result
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
                                    HashMap<String, DatatableProperties> datatableproperties = clownfishutil.getDatatableproperties(renderctx.getPostmap());
                                    EmailProperties emailproperties = clownfishutil.getEmailproperties(renderctx.getPostmap());
                                    HashMap<String, DatatableNewProperties> datatablenewproperties = clownfishutil.getDatatablenewproperties(renderctx.getPostmap());
                                    HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = clownfishutil.getDatatabledeleteproperties(renderctx.getPostmap());
                                    HashMap<String, DatatableUpdateProperties> datatableupdateproperties = clownfishutil.getDatatableupdateproperties(renderctx.getPostmap());
                                    manageSessionVariables(renderctx.getPostmap());
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
                                    metainfomap.put("referrer", renderctx.getReferrer());
                                    if (null != renderctx.getClientinfo()) {
                                        if (null != renderctx.getClientinfo().getHostname()) metainfomap.put("hostname", renderctx.getClientinfo().getHostname());
                                        if (null != renderctx.getClientinfo().getIpadress()) metainfomap.put("ipaddress", renderctx.getClientinfo().getIpadress());
                                    }

                                    // instantiate Template Beans
                                    btx.setNetworkbean(new NetworkTemplateBean());
                                    btx.setWebservicebean(new WebServiceTemplateBean());
                                    btx.setWebsocketbean(new WebSocketTemplateBean());
                                    btx.getWebsocketbean().setWebsocketPort(websocketPort);
                                    btx.setUploadbean(new UploadTemplateBean());
                                    btx.getUploadbean().init(cftemplateService, propertyUtil, cfdatasourceService);
                                    if (null != renderctx.getFileitems()) {
                                        btx.getUploadbean().setFileitemlist(renderctx.getFileitems());
                                    }

                                    btx.setEmailbean(new EmailTemplateBean());
                                    btx.getEmailbean().init(propertyUtil.getPropertymap(), mailUtil, propertyUtil);
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
                                        btx.setSapbean(new SAPTemplateBean());
                                        btx.getSapbean().init(sapc, sitesaprfclist, rpytableread, renderctx.getPostmap());
                                    }

                                    btx.setDatabasebean(new DatabaseTemplateBean(propertyUtil));
                                    btx.setDownloadbean(new DownloadTemplateBean(propertyUtil));
                                    btx.setImportbean(new ImportTemplateBean());
                                    btx.setPdfbean(new PDFTemplateBean());
                                    btx.getPdfbean().init(pdfUtil);
                                    if (!sitedatasourcelist.isEmpty()) {
                                        btx.getDatabasebean().init(sitedatasourcelist, cfdatasourceService);
                                        btx.getImportbean().init(sitedatasourcelist, cfdatasourceService);
                                    }
                                    btx.setExternalclassproviderbean(new ExternalClassProvider(cfclassCompiler));
                                    contentUtil.init(markdownUtil, renderctx.getName(), renderctx.getUrlParams());
                                    btx.setContentbean(new ContentTemplateBean(propertyUtil, contentUtil));
                                    btx.getContentbean().init(cfclasscontentService, cfattributcontentService, cflistService, cflistcontentService, cfclassService, cfassetlistService, cfassetlistcontentService, cfassetService, useHibernate);
                                    btx.setJsonatorbean(new JSONatorBean());
                                    btx.getJsonatorbean().init(cftemplateService);
                                    
                                    Map<String, Object> templateBeans = new HashMap<String, Object>();
                                    templateBeans.put("networkBean", btx.getNetworkbean());
                                    templateBeans.put("webserviceBean", btx.getWebservicebean());
                                    templateBeans.put("websocketBean", btx.getWebsocketbean());
                                    templateBeans.put("uploadBean", btx.getUploadbean());
                                    templateBeans.put("emailBean", btx.getEmailbean());
                                    templateBeans.put("sapBean", btx.getSapbean());
                                    templateBeans.put("databaseBean", btx.getDatabasebean());
                                    templateBeans.put("downloadBean", btx.getDownloadbean());
                                    templateBeans.put("importBean", btx.getImportbean());
                                    templateBeans.put("pdfBean", btx.getPdfbean());
                                    templateBeans.put("classBean", btx.getExternalclassproviderbean());
                                    templateBeans.put("contentBean", btx.getContentbean());
                                    templateBeans.put("jsonatorBean", btx.getJsonatorbean());
                                    
                                    renderctx.setSitecontentmap(sitecontentmap);
                                    renderctx.setMetainfomap(metainfomap);
                                    renderctx.setParametermap(parametermap);
                                    ClownfishResponse resp = null; 
                                    if (engineResult.isScripted()) {                                                                           // NORMAL Template
                                        switch (cftemplate.getScriptlanguage()) {
                                            case 0 -> {
                                                resp = freemarkerService.populateAndRender(engineResult, renderctx, cftemplate, templateBeans, cfstylesheet, cfjavascript, btx, cfsite);
                                            }
                                            case 1 -> {
                                                // VELOCITY
                                                resp = velocityService.populateAndRender(engineResult, renderctx, cftemplate, templateBeans, cfstylesheet, cfjavascript, btx, cfsite);
                                            }
                                            default -> // HTML
                                            {
                                            }
                                        }
                                        // HTML
                                    } else {                                                                                // LAYOUT Template
                                        if (1 == cftemplate.getType()) {
                                            String output = "";
                                            try {
                                                output = manageLayout(cfsite, cftemplate.getName(), cftemplate.getContent(), cfstylesheet, cfjavascript, parametermap, btx);
                                            } catch (ClownfishTemplateException ex) {
                                                //LOGGER.error(ex.getMessage());
                                                cfresponse.setErrorcode(5);
                                                cfresponse.setOutput("ClownfishTemplateException");
                                                cfresponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                                                return cfresponse;
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
                                        if (null != resp) {
                                            cfresponse.setOutput(htmlcompressor.compress(resp.getOutput()));
                                        } else {
                                            cfresponse.setOutput(htmlcompressor.compress(out.toString()));
                                        }
                                        //LOGGER.info("END makeResponse: " + name);
                                        return cfresponse;
                                    } else {
                                        cfresponse.setErrorcode(0);
                                        if (null != resp) {
                                            cfresponse.setOutput(resp.getOutput());
                                        } else {
                                            cfresponse.setOutput(out.toString());
                                        }
                                        //LOGGER.info("END makeResponse: " + name);
                                        return cfresponse;
                                    }
                                } else {
                                    cfresponse.setErrorcode(4);
                                    cfresponse.setOutput("Template not set");
                                    return cfresponse;
                                }
                            } catch (NoResultException ex) {
                                LOGGER.info("Exception: " + ex);
                                cfresponse.setErrorcode(1);
                                cfresponse.setOutput("No template");
                                //LOGGER.info("END makeResponse: " + name);
                                return cfresponse;
                            }
                        }
                    } else {
                        cfresponse.setErrorcode(2);
                        cfresponse.setOutput("Only for Job calling");
                        return cfresponse;
                    }
                }   else {
                    cfresponse.setErrorcode(4);
                    cfresponse.setOutput("Offline");
                    cfresponse.setRelocation(cfsite.getLoginsiteref().getName());
                    return cfresponse;
                }
            } else {
                cfresponse.setErrorcode(3);
                cfresponse.setOutput("No access");
                cfresponse.setRelocation(cfsite.getLoginsiteref().getName());
                return cfresponse;
            } 
        } catch (IOException ex) {
            cfresponse.setErrorcode(1);
            cfresponse.setOutput(ex.getMessage());
            //LOGGER.info("END makeResponse: " + name);
            return cfresponse;
        }
    }
    
    private String manageLayout(CfSite cfsite, String templatename, String templatecontent, String cfstylesheet, String cfjavascript, Map parametermap, BeanContext btx) throws ClownfishTemplateException {
        boolean preview = false;        // is preview
        if (parametermap.containsKey("preview")) {    // check mode for display (preview or productive)
            if (parametermap.get("preview").toString().compareToIgnoreCase("true") == 0) {
                preview = true;
            }
        }
        
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
                    content = layoutService.interpretscript(content, cfdivtemplate, cfstylesheet, cfjavascript, parametermap, modus, rendercontext, btx);
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
                    if ((null != sitetree) && (null != sitetree.getLayout())) {
                        for (CfDiv comp_div : templateUtil.getLayout().getDivs()) {
                            if ((0 == cfdiv.getName().compareToIgnoreCase(comp_div.getName())) && (sitetree.getVisibleMap().get(comp_div.getId()))) {
                                div.html(content);
                            }
                        }
                    } else {
                        for (CfDiv comp_div : templateUtil.getLayout().getDivs()) {
                            if (0 == cfdiv.getName().compareToIgnoreCase(comp_div.getName())) {
                                div.html(content);
                            }
                        }
                    }
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
        if (null != staticsite) {
            return !staticsite.isOffline();
        } else {
            return true;
        }
    }
    
    /**
     * getStaticSite
     * 
     */
    private ClownfishResponse getStaticSite(RenderContext rc) {
        ClownfishResponse cfResponse = new ClownfishResponse();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(folderUtil.getStatic_folder() + File.separator + rc.getName()), "UTF-8"));
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
            CfSite cfsite = cfsiteService.findByName(rc.getName());
            String aliasname = cfsite.getAliaspath();
            ClownfishResponse cfStaticResponse;
            try {
                rc.setName(rc.getName());
                rc.setPostmap(rc.getPostmap());
                rc.setUrlParams(rc.getUrlParams());
                rc.setMakestatic(true);
                rc.setFileitems(null);
                rc.setClientinfo(null);
                rc.setReferrer("");
                cfStaticResponse = renderPage(rc);
                
                if (0 == cfStaticResponse.getErrorcode()) {
                    if (!rc.getUrlParams().isEmpty()) {
                        staticSiteUtil.generateStaticSite(rc.getName(), "", cfStaticResponse.getOutput(), cfassetService, folderUtil);
                    } else {
                        staticSiteUtil.generateStaticSite(rc.getName(), aliasname, cfStaticResponse.getOutput(), cfassetService, folderUtil);
                    }
                } else {
                    deleteStaticSite(rc.getName(), rc.getUrlParams());
                }
            } catch (PageNotFoundException ex1) {
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
     * sendRespondMail
     * 
     */
    private void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        MailUtil mailutil = new MailUtil(propertyUtil);
        mailutil.sendRespondMail(mailto, subject, mailbody);
    }
}
