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
package io.clownfish.clownfish.beans;

import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.datamodels.CfDiv;
import io.clownfish.clownfish.datamodels.CfLayout;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSiteassetlist;
import io.clownfish.clownfish.dbentities.CfSiteassetlistPK;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitecontentPK;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasourcePK;
import io.clownfish.clownfish.dbentities.CfSitekeywordlist;
import io.clownfish.clownfish.dbentities.CfSitekeywordlistPK;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.dbentities.CfSitelistPK;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfSitesaprfcPK;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.lucene.SourceIndexer;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.models.RfcFunction;
import io.clownfish.clownfish.sap.models.RfcGroup;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfPropertyService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSiteassetlistService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitekeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.JavascriptUtil;
import io.clownfish.clownfish.utils.SiteUtil;
import io.clownfish.clownfish.utils.StylesheetUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("session")
@Named("sitetree")
@Component
public class SiteTreeBean implements Serializable {
    @Value("${sapconnection.file}") String SAPCONNECTION;
    private static SAPConnection sapc = null;
    
    private transient @Getter @Setter TreeNode root;
    private transient @Getter @Setter TreeNode selectedNode = null;
    private @Getter @Setter String siteName;
    private @Getter @Setter CfSite selectedSite = null;
    private @Getter @Setter CfTemplate selectedTemplate = null;
    private @Getter @Setter CfStylesheet selectedStylesheet = null;
    private @Getter @Setter CfJavascript selectedJavascript = null;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter List<CfDatasource> datasources;
    private @Getter @Setter List<CfDatasource> selectedDatasources;
    private @Getter @Setter List<CfList> contentlist;
    private @Getter @Setter List<CfList> selectedContentlist;
    private transient @Getter @Setter List<CfSitesaprfc> saprfclist = null;
    private @Getter @Setter CfSitesaprfc selectedrfc = null;
    private @Getter @Setter List<RfcGroup> rfcgrouplist;
    private @Getter @Setter String rfcgroup;
    private @Getter @Setter RfcGroup selectedrfcgroup = null;
    private @Getter @Setter List<RfcFunction> rfcfunctionlist;
    private @Getter @Setter RfcFunction selectedrfcfunction = null;
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter List<CfClasscontent> selectedClasscontentlist;
    private @Getter @Setter List<CfAssetlist> assetlist;
    private @Getter @Setter List<CfAssetlist> selectedAssetlist;
    private @Getter @Setter List<CfKeywordlist> keywordlist;
    private @Getter @Setter List<CfKeywordlist> selectedKeywordlist;
    private @Getter @Setter int sitehtmlcompression;
    private @Getter @Setter int sitegzip;
    private @Getter @Setter boolean sitejob;
    private @Getter @Setter boolean sitestatic;
    private @Getter @Setter boolean sitesearchrelevant;
    private @Getter @Setter boolean sitemap;
    private @Getter @Setter boolean searchresult;
    private @Getter @Setter String siteTitle;
    private @Getter @Setter String siteDescription;
    private @Getter @Setter String aliaspath;
    private @Getter @Setter String characterEncoding;
    private @Getter @Setter String contentType;
    private @Getter @Setter String locale;
    private transient @Getter @Setter Map<String, String> propertymap = null;
    private @Getter @Setter boolean sapSupport = false;
    private @Getter @Setter CfLayout layout = null;
    private @Getter @Setter CfDiv selectedDiv = null;
    private @Getter @Setter CfTemplate selectedDivTemplate = null;
    private @Getter @Setter boolean contenteditable;
    private @Getter @Setter String selected_contentclass = null;
    private @Getter @Setter String selected_datalisttclass = null;
    private @Getter @Setter String selected_asset = null;
    private @Getter @Setter String selected_assetlist = null;
    private @Getter @Setter String selected_keywordlist = null;
    
    private @Getter @Setter boolean showContent;
    private @Getter @Setter CfClasscontent current_classcontent = null;
    private @Getter @Setter List<CfClasscontent> current_classcontentlist = null;
    private @Getter @Setter List<CfClasscontent> filteredContent;
    
    private @Getter @Setter boolean showDatalist;
    private @Getter @Setter CfList current_list = null;
    private @Getter @Setter List<CfList> current_datalist = null;
    private @Getter @Setter List<CfList> filteredList;
    
    private @Getter @Setter boolean showAsset;
    private @Getter @Setter CfAsset current_asset = null;
    private @Getter @Setter List<CfAsset> current_assetlist = null;
    private @Getter @Setter List<CfAsset> filteredAsset;
    
    private @Getter @Setter boolean showAssetLibrary;
    private @Getter @Setter CfAssetlist current_assetlibrary = null;
    private @Getter @Setter List<CfAssetlist> current_assetlibrarylist = null;
    private @Getter @Setter List<CfAssetlist> filteredAssetlibrary;
    
    private @Getter @Setter boolean showKeywordLibrary;
    private @Getter @Setter CfKeywordlist current_keywordlibrary = null;
    private @Getter @Setter List<CfKeywordlist> current_keywordlibrarylist = null;
    private @Getter @Setter List<CfKeywordlist> filteredKeywordlibrary;

    private @Getter @Setter CfLayoutcontent current_layoutcontent = null;
    
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfTemplateversionService cftemplateversionService;
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfStylesheetversionService cfstylesheetversionService;
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired transient CfJavascriptversionService cfjavascriptversionService;
    @Autowired transient CfSiteService cfsiteService;
    @Autowired transient CfDatasourceService cfdatasourceService;
    @Autowired transient CfSitedatasourceService cfsitedatasourceService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired transient CfSiteassetlistService cfsiteassetlistService;
    @Autowired transient CfSitekeywordlistService cfsitekeywordlistService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfKeywordlistcontentService cfkeywordlistcontentService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfSitelistService cfsitelistService;
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfKeywordlistService cfkeywordlistService;
    @Autowired CfLayoutcontentService cflayoutcontentService;
    @Autowired transient CfSitesaprfcService cfsitesaprfcService;
    @Autowired transient CfPropertyService cfpropertyService;
    @Autowired transient PropertyList propertylist;
    @Autowired transient TemplateList templatelist;
    @Autowired transient StylesheetList stylesheetlist;
    @Autowired transient JavascriptList javascriptlist;
    @Autowired private @Getter @Setter TemplateUtil templateUtility;
    @Autowired private @Getter @Setter StylesheetUtil stylesheetUtility;
    @Autowired private @Getter @Setter JavascriptUtil javascriptUtility;
    @Autowired private @Getter @Setter ClassUtil classUtility;
    @Autowired transient FolderUtil folderUtil;
    @Autowired transient SiteUtil siteUtil;
    private SourceIndexer sourceindexer;
    private @Getter @Setter String iframeurl = "";
    @Autowired transient Clownfish clownfish;
    private transient @Getter @Setter List<CfAttributcontent> attributcontentlist = null;
    private @Getter @Setter String previewContentOutput = "";
    private @Getter @Setter long previewAssetOutput = 0;
    private @Getter @Setter List<CfAsset> previewAssetlistOutput = new ArrayList<>();
    private @Getter @Setter String previewDatalistOutput = "";
    private @Getter @Setter List<CfKeyword> previewKeywordlistOutput = new ArrayList<>();
    
    final transient Logger LOGGER = LoggerFactory.getLogger(SiteTreeBean.class);
    
    @PostConstruct
    public void init() {
        LOGGER.info("INIT SITETREE START");
        if (null != clownfish) {
            clownfish.setSitetree(this);
        }
        if (null == sourceindexer) {
            sourceindexer = new SourceIndexer();
        }
        propertymap = propertylist.fillPropertyMap();
        String sapSupportProp = propertymap.get("sap_support");
        if (sapSupportProp == null) {
            sapSupport = false;
        } else {
            if (sapSupportProp.compareToIgnoreCase("TRUE") == 0) {
                sapSupport = true;
                LOGGER.info("SAP SUPPORT");
            }
            if (sapSupport) {
                sapc = new SAPConnection(SAPCONNECTION, "Clownfish4");
                rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
            }
        }
        //root = new DefaultTreeNode("Root", null);
        loadTree();
        datasources = cfdatasourceService.findAll();
        contentlist = cflistService.findByMaintenance(true);
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        assetlist = cfassetlistService.findAll();
        keywordlist = cfkeywordlistService.findAll();
        
        selectedDatasources = new ArrayList<>();
        selectedContentlist = new ArrayList<>();
        selectedClasscontentlist = new ArrayList<>();
        selectedAssetlist = new ArrayList<>();
        selectedKeywordlist = new ArrayList<>();
        locale = propertymap.get("response_locale");
        contentType = propertymap.get("response_contenttype");
        characterEncoding = propertymap.get("response_characterencoding");
        contenteditable = false;
        searchresult = false;
        showContent = false;
        showDatalist = false;
        showAssetLibrary = false;
        showKeywordLibrary = false;
        templatelist.setSitetree(this);
        javascriptlist.setSitetree(this);
        stylesheetlist.setSitetree(this);
        classUtility.setSitetree(this);
        LOGGER.info("INIT SITETREE END");
    }
    
    public void initDatasources() {
        datasources = cfdatasourceService.findAll();
    }
    
    public void initContentlist() {
        contentlist = cflistService.findByMaintenance(true);
    }
    
    public void initClassContentlist() {
        classcontentlist = cfclasscontentService.findByMaintenance(true);
    }
    
    public void initAssetlibrarylist() {
        assetlist = cfassetlistService.findAll();
    }
    
    public void initKeywordlibrarylist() {
        keywordlist = cfkeywordlistService.findAll();
    }
    
    public void onRefreshAll() {
        datasources = cfdatasourceService.findAll();
        contentlist = cflistService.findByMaintenance(true);
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        assetlist = cfassetlistService.findAll();
    }
    
    public void onRefreshSelection() {
        if (null != selectedTemplate) {
            selectedTemplate = cftemplateService.findById(selectedTemplate.getId());
        }
        if (null != selectedJavascript) {
            selectedJavascript = cfjavascriptService.findById(selectedJavascript.getId());
        }
        if (null != selectedStylesheet) {
            selectedStylesheet = cfstylesheetService.findById(selectedStylesheet.getId());
        }
        if (null != current_classcontent) {
            onChangeLayoutContent();
            current_classcontent = cfclasscontentService.findById(current_classcontent.getId());
            current_classcontent.getClassref().setTemplateref(current_classcontent.getClassref().getTemplateref());
        }
    }

    private void fillChildren(long parentid, TreeNode node) {
        List<CfSite> sitelist = cfsiteService.findByParentref(parentid);
                
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            node.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
    
    public void loadTree() {
        root = new DefaultTreeNode("Root", null);
        List<CfSite> sitelist = cfsiteService.findByParentref(0L);
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            root.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
 
    public String getTemplate() {
        if (null != selectedTemplate) {
            return templateUtility.getVersion(selectedTemplate.getId(), cftemplateversionService.findMaxVersion(selectedTemplate.getId()));
        } else {
            return "";
        }
    }
    
    public String getStylesheet() {
        if (null != selectedStylesheet) {
            return stylesheetUtility.getVersion(selectedStylesheet.getId(), cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId()));
        } else {
            return "";
        }
    }
    
    public String getJavascript() {
        if (null != selectedJavascript) {
            return javascriptUtility.getVersion(selectedJavascript.getId(), cfjavascriptversionService.findMaxVersion(selectedJavascript.getId()));
        } else {
            return "";
        }
    }

    public void onRefresh(ActionEvent actionEvent) {
        init();
    }
    
    public void onDragDrop(TreeDragDropEvent event) {
        TreeNode dragNode = event.getDragNode();
        TreeNode dropNode = event.getDropNode();
        int dropIndex = event.getDropIndex();
        CfSite dragsite = (CfSite) dragNode.getData();
        if (dropNode.getParent() != null) {
            CfSite dropsite = (CfSite) dropNode.getData();
            dragsite.setParentref(BigInteger.valueOf(dropsite.getId()));
            cfsiteService.edit(dragsite);
        } else {
            dragsite.setParentref(BigInteger.ZERO);
            cfsiteService.edit(dragsite);
        }
        
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Dragged " + dragNode.getData(), "Dropped on " + dropNode.getData() + " at " + dropIndex);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onUnselect(NodeUnselectEvent event) {
        selectedNode = event.getTreeNode();
        selectedSite = null;
        
        selectedTemplate = null;
        selectedStylesheet = null;
        selectedJavascript = null;
        siteName = "";
        siteTitle = "";
        siteDescription = "";
        aliaspath = "";
        sitehtmlcompression = 0;
        characterEncoding = "";
        contentType = "";
        locale = "";
        selectedDatasources.clear();
        selectedContentlist.clear();
        selectedClasscontentlist.clear();
        selectedAssetlist.clear();
        selectedKeywordlist.clear();
        sitejob = false;
        sitesearchrelevant = false;
        searchresult = false;
        sitemap = false;
        sitestatic = false;
        newButtonDisabled = false;
        contenteditable = false;
        selected_contentclass = null;
        selected_datalisttclass = null;
        selected_asset = null;
        selected_assetlist = null;
        selected_keywordlist = null;
        showContent = false;
        showDatalist = false;
        showAsset = false;
        showAssetLibrary = false;
        showKeywordLibrary = false;
        selectedDiv = null;
    }
    
    public void onSelect(NodeSelectEvent event) {
        selectedNode = event.getTreeNode();
        selectedSite = (CfSite) selectedNode.getData();
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        if (null != selectedSite.getTemplateref()) {
            CfTemplate template = cftemplateService.findById(selectedSite.getTemplateref().longValue());
            int idx = templatelist.getTemplateListe().indexOf(template);
            selectedTemplate = templatelist.getTemplateListe().get(idx);
            
            iframeurl = selectedSite.getName() + "?preview=true";
            
            selectedDiv = null;
            showContent = false;
            showDatalist = false;
            showAsset = false;
            showAssetLibrary = false;
            showKeywordLibrary = false;
            if (template.isLayout()) {
                contenteditable = true;
                FacesMessage message = new FacesMessage("LAYOUT TEMPLATE");
                FacesContext.getCurrentInstance().addMessage(null, message);
                //List<CfLayoutcontent> layoutcontent = cflayoutcontentService.findBySiteref(selectedSite.getId());
                layout = new CfLayout(template.getName());
                Document doc = Jsoup.parse(template.getContent());
                Elements divs = doc.getElementsByAttribute("template");
                for (Element div : divs) {
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
                    layout.getDivArray().put(div.attr("id"), cfdiv);
                }
            } else {
                contenteditable = false;
                selected_contentclass = null;
                selected_datalisttclass = null;
                selected_asset = null;
                selected_assetlist = null;
                selected_keywordlist = null;
            }
            
        } else {
            selectedTemplate = null;
        }
        if (null != selectedSite.getStylesheetref()) {
            CfStylesheet styleshet = cfstylesheetService.findById(selectedSite.getStylesheetref().longValue());
            int idx = stylesheetlist.getStylesheetListe().indexOf(styleshet);
            selectedStylesheet = stylesheetlist.getStylesheetListe().get(idx);
        } else {
            selectedStylesheet = null;
        }
        if (null != selectedSite.getJavascriptref()) {
            CfJavascript javascript = cfjavascriptService.findById(selectedSite.getJavascriptref().longValue());
            int idx = javascriptlist.getJavascriptListe().indexOf(javascript);
            selectedJavascript = javascriptlist.getJavascriptListe().get(idx);
        } else {
            selectedJavascript = null;
        }
        selectedDatasources.clear();
        List<CfSitedatasource> selectedSiteDatasources = cfsitedatasourceService.findBySiteref(selectedSite.getId());
        for (CfSitedatasource sitedatasource : selectedSiteDatasources) {
            CfDatasource ds = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
            selectedDatasources.add(ds);
        }
        
        selectedContentlist.clear();
        List<CfSitelist> selectedSitecontentlist = cfsitelistService.findBySiteref(selectedSite.getId());
        for (CfSitelist sitelist : selectedSitecontentlist) {
            CfList cl = cflistService.findById(sitelist.getCfSitelistPK().getListref());
            selectedContentlist.add(cl);
        }
        
        selectedClasscontentlist.clear();
        List<CfSitecontent> selectedClasscontentliste = cfsitecontentService.findBySiteref(selectedSite.getId());
        for (CfSitecontent sitecontent : selectedClasscontentliste) {
            CfClasscontent cc = cfclasscontentService.findById(sitecontent.getCfSitecontentPK().getClasscontentref());
            selectedClasscontentlist.add(cc);
        }
        
        selectedAssetlist.clear();
        List<CfSiteassetlist> selectedAssetliste = cfsiteassetlistService.findBySiteref(selectedSite.getId());
        for (CfSiteassetlist siteassetlist : selectedAssetliste) {
            CfAssetlist csa = cfassetlistService.findById(siteassetlist.getCfSiteassetlistPK().getAssetlistref());
            selectedAssetlist.add(csa);
        }
        
        selectedKeywordlist.clear();
        List<CfSitekeywordlist> selectedKeywordliste = cfsitekeywordlistService.findBySiteref(selectedSite.getId());
        for (CfSitekeywordlist sitekeywordlist : selectedKeywordliste) {
            CfKeywordlist kwl = cfkeywordlistService.findById(sitekeywordlist.getCfSitekeywordlistPK().getKeywordlistref());
            selectedKeywordlist.add(kwl);
        }
        
        siteName = selectedSite.getName();
        siteTitle = selectedSite.getTitle();
        siteDescription = selectedSite.getDescription();
        sitejob = selectedSite.isJob();
        sitesearchrelevant = selectedSite.isSearchrelevant();
        sitemap = selectedSite.isSitemap();
        sitestatic = selectedSite.isStaticsite();
        searchresult = selectedSite.isSearchresult();
        aliaspath = selectedSite.getAliaspath();
        sitehtmlcompression = selectedSite.getHtmlcompression();
        characterEncoding = selectedSite.getCharacterencoding();
        contentType = selectedSite.getContenttype();
        locale = selectedSite.getLocale();
        saprfclist = cfsitesaprfcService.findBySiteref(selectedSite.getId());
        newButtonDisabled = true;
        
        FacesMessage message = new FacesMessage("Selected " + selectedSite.getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedSite) {
            cfsiteService.delete(selectedSite);
            loadTree();
            
            FacesMessage message = new FacesMessage("Deleted " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChange(ActionEvent actionEvent) {
        if (null != selectedSite) {
            if (null != selectedStylesheet) {
                selectedSite.setStylesheetref(BigInteger.valueOf(selectedStylesheet.getId().intValue()));
            } else {
                selectedSite.setStylesheetref(null);
            }
            if (null != selectedTemplate) {
                selectedSite.setTemplateref(BigInteger.valueOf(selectedTemplate.getId().intValue()));
            } else {
                selectedSite.setTemplateref(null);
            }
            if (null != selectedJavascript) {
                selectedSite.setJavascriptref(BigInteger.valueOf(selectedJavascript.getId().intValue()));
            } else {
                selectedSite.setJavascriptref(null);
            }
            
            // Delete siteresources first
            List<CfSitedatasource> sitedatasourceList = cfsitedatasourceService.findBySiteref(selectedSite.getId());
            for (CfSitedatasource sitedatasource : sitedatasourceList) {
                cfsitedatasourceService.delete(sitedatasource);
            }
            // Add selected siteresources
            if (!selectedDatasources.isEmpty()) {
                for (CfDatasource datasource : selectedDatasources) {
                    CfSitedatasource sitedatasource = new CfSitedatasource();
                    CfSitedatasourcePK cfsitedatasourcePK = new CfSitedatasourcePK();
                    cfsitedatasourcePK.setSiteref(selectedSite.getId());
                    cfsitedatasourcePK.setDatasourceref(datasource.getId());
                    sitedatasource.setCfSitedatasourcePK(cfsitedatasourcePK);
                    cfsitedatasourceService.create(sitedatasource);
                }
            }
            
            // Delete sitelists first
            List<CfSitelist> sitelists = cfsitelistService.findBySiteref(selectedSite.getId());
            for (CfSitelist sitelist : sitelists) {
                cfsitelistService.delete(sitelist);
            }
            // Add selected sitelists
            if (!selectedContentlist.isEmpty()) {
                for (CfList contentList : selectedContentlist) {
                    CfSitelist sitelist = new CfSitelist();
                    CfSitelistPK cfsitelistPK = new CfSitelistPK();
                    cfsitelistPK.setSiteref(selectedSite.getId());
                    cfsitelistPK.setListref(contentList.getId());
                    sitelist.setCfSitelistPK(cfsitelistPK);
                    cfsitelistService.create(sitelist);
                }
            }
            
            // Delete sitecontent first
            List<CfSitecontent> contentlists = cfsitecontentService.findBySiteref(selectedSite.getId());
            for (CfSitecontent content : contentlists) {
                cfsitecontentService.delete(content);
            }
            // Add selected sitecontent
            if (!selectedClasscontentlist.isEmpty()) {
                for (CfClasscontent content : selectedClasscontentlist) {
                    CfSitecontent sitecontent = new CfSitecontent();
                    CfSitecontentPK cfsitecontentPK = new CfSitecontentPK();
                    cfsitecontentPK.setSiteref(selectedSite.getId());
                    cfsitecontentPK.setClasscontentref(content.getId());
                    sitecontent.setCfSitecontentPK(cfsitecontentPK);
                    cfsitecontentService.create(sitecontent);
                }
            }
            
            // Delete siteassetlist first
            List<CfSiteassetlist> siteassetlists = cfsiteassetlistService.findBySiteref(selectedSite.getId());
            for (CfSiteassetlist assetList : siteassetlists) {
                cfsiteassetlistService.delete(assetList);
            }
            // Add selected sitecontent
            if (!selectedAssetlist.isEmpty()) {
                for (CfAssetlist content : selectedAssetlist) {
                    CfSiteassetlist siteassetlist = new CfSiteassetlist();
                    CfSiteassetlistPK cfsitecontentPK = new CfSiteassetlistPK();
                    cfsitecontentPK.setSiteref(selectedSite.getId());
                    cfsitecontentPK.setAssetlistref(content.getId());
                    siteassetlist.setCfSiteassetlistPK(cfsitecontentPK);
                    cfsiteassetlistService.create(siteassetlist);
                }
            }
            
            // Delete sitekeywordlist first
            List<CfSitekeywordlist> sitekeywordlists = cfsitekeywordlistService.findBySiteref(selectedSite.getId());
            for (CfSitekeywordlist keywordList : sitekeywordlists) {
                cfsitekeywordlistService.delete(keywordList);
            }
            // Add selected sitecontent
            if (!selectedKeywordlist.isEmpty()) {
                for (CfKeywordlist content : selectedKeywordlist) {
                    CfSitekeywordlist sitekeywordlist = new CfSitekeywordlist();
                    CfSitekeywordlistPK cfsitecontentPK = new CfSitekeywordlistPK();
                    cfsitecontentPK.setSiteref(selectedSite.getId());
                    cfsitecontentPK.setKeywordlistref(content.getId());
                    sitekeywordlist.setCfSitekeywordlistPK(cfsitecontentPK);
                    cfsitekeywordlistService.create(sitekeywordlist);
                }
            }
            
            selectedSite.setName(siteName);
            selectedSite.setHtmlcompression(sitehtmlcompression);
            selectedSite.setCharacterencoding(characterEncoding);
            selectedSite.setContenttype(contentType);
            selectedSite.setLocale(locale);
            selectedSite.setAliaspath(aliaspath);
            selectedSite.setTitle(siteTitle);
            selectedSite.setDescription(siteDescription);
            selectedSite.setJob(sitejob);
            selectedSite.setSearchrelevant(sitesearchrelevant);
            selectedSite.setSearchresult(searchresult);
            selectedSite.setSitemap(sitemap);
            selectedSite.setStaticsite(sitestatic);
            cfsiteService.edit(selectedSite);
            loadTree();
            
            FacesMessage message = new FacesMessage("Changed " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChangeTemplate() {
        if (null != selectedTemplate) {
            
        }
    }
    
    public void onChangeStylesheet() {
        if (null != selectedStylesheet) {
            
        }
    }
    
    public void onChangeJavascript() {
        if (null != selectedStylesheet) {
            
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfsiteService.findByName(siteName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = siteName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfSite newsite = new CfSite();
            newsite.setHitcounter(BigInteger.ZERO);
            newsite.setName(siteName);
            if (null != selectedSite) {
                newsite.setParentref(BigInteger.valueOf(selectedSite.getId()));
            } else {
                newsite.setParentref(BigInteger.ZERO);
            }
            if (null != selectedTemplate) {
                newsite.setTemplateref(BigInteger.valueOf(selectedTemplate.getId()));
            }
            if (null != selectedStylesheet) {
                newsite.setStylesheetref(BigInteger.valueOf(selectedStylesheet.getId()));
            }
            if (null != selectedJavascript) {
                newsite.setJavascriptref(BigInteger.valueOf(selectedJavascript.getId()));
            }
            newsite.setHtmlcompression(sitehtmlcompression);
            newsite.setContenttype(contentType);
            newsite.setCharacterencoding(characterEncoding);
            newsite.setLocale(locale);
            newsite.setAliaspath(siteName);
            newsite.setTitle(siteTitle);
            newsite.setDescription(siteDescription);
            newsite.setJob(sitejob);
            newsite.setSearchrelevant(sitesearchrelevant);
            newsite.setSearchresult(searchresult);
            newsite.setSitemap(sitemap);
            newsite.setStaticsite(sitestatic);
            newsite.setShorturl(siteUtil.generateShorturl());
            selectedSite = cfsiteService.create(newsite);
            loadTree();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onPublish(ActionEvent actionEvent) {
        if (null != folderUtil.getStatic_folder()) {
            File file = new File(folderUtil.getStatic_folder() + File.separator + selectedSite.getName());
            try {
                Files.deleteIfExists(file.toPath());
                FacesMessage message = new FacesMessage("Deleted static site for " + selectedSite.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        
        if (null != selectedSite) {
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiteref(selectedSite.getId());
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                layoutcontent.setContentref(layoutcontent.getPreview_contentref());
                cflayoutcontentService.edit(layoutcontent);
            }
            FacesMessage message = new FacesMessage("Published " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onDeleteStaticSite(ActionEvent actionEvent) {
        if (null != folderUtil.getStatic_folder()) {
            File file = new File(folderUtil.getStatic_folder() + File.separator + selectedSite.getName());
            try {
                Files.deleteIfExists(file.toPath());
                FacesMessage message = new FacesMessage("Deleted static site for " + selectedSite.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    public void onChangeRfCGroupInput() {
        if (!rfcgroup.isEmpty()) {
            selectedrfcgroup = null;
            rfcfunctionlist = new RFC_FUNCTION_SEARCH(sapc).getRfcFunctionsList(rfcgroup);
        }
    }
    
    public void onChangeRfcGroup() {
        if (null != selectedrfcgroup) {
            rfcgroup = selectedrfcgroup.getName();
            rfcfunctionlist = new RFC_FUNCTION_SEARCH(sapc).getRfcFunctionsList(selectedrfcgroup.getName());
        }
    }
    
    public void onChangeRfcFunction() {
        if (null != selectedrfcfunction) {
            
        }
    }
    
    public void onDeleteRfc(ActionEvent actionEvent) {
        if (null != selectedrfc) {
            cfsitesaprfcService.delete(selectedrfc);
            saprfclist = cfsitesaprfcService.findBySiteref(selectedSite.getId());
        }
    }
    
    public void onNewRfc(ActionEvent actionEvent) {
        if ((null != selectedrfcgroup) && (null != selectedrfcfunction)) {
            CfSitesaprfc sitesaprfc = new CfSitesaprfc();
            CfSitesaprfcPK cfsitesaprfcPK = new CfSitesaprfcPK();
            cfsitesaprfcPK.setSiteref(selectedSite.getId());
            cfsitesaprfcPK.setRfcgroup(selectedrfcgroup.getName());
            cfsitesaprfcPK.setRfcfunction(selectedrfcfunction.getName());
            sitesaprfc.setCfSitesaprfcPK(cfsitesaprfcPK);
            cfsitesaprfcService.create(sitesaprfc);
            saprfclist = cfsitesaprfcService.findBySiteref(selectedSite.getId());
        }
    }
    
    public void onRfcSelect(SelectEvent event) {
        selectedrfc = (CfSitesaprfc) event.getObject();
        selectedrfcgroup = getAsRfcGroup(selectedrfc.getCfSitesaprfcPK().getRfcgroup());
        selectedrfcfunction = getAsRfcFunction(selectedrfc.getCfSitesaprfcPK().getRfcfunction());
    }
    
    private RfcGroup getAsRfcGroup(String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            if (sapSupport) {
                rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
                for (RfcGroup rfcGroup : rfcgrouplist) {
                    if (rfcGroup.getName().compareToIgnoreCase(value) == 0 ) {
                        return rfcGroup;
                    }
                }
            }
            return null;
        }
    }
    
    private RfcFunction getAsRfcFunction(String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            rfcfunctionlist = new RFC_FUNCTION_SEARCH(sapc).getRfcFunctionsList(selectedrfc.getCfSitesaprfcPK().getRfcgroup());
            for (RfcFunction rfcfunction : rfcfunctionlist) {
                if (rfcfunction.getName().compareToIgnoreCase(value) == 0 ) {
                    return rfcfunction;
                }
            }
            return null;
        }
    }
    
    public void onDivSelect(SelectEvent event) {
        selectedDiv = (CfDiv) event.getObject();
        if (null != selectedDiv) {
            showContent = false;
            showDatalist = false;
            showAsset = false;
            showAssetLibrary = false;
            showKeywordLibrary = false;
            selectedDivTemplate = cftemplateService.findByName(selectedDiv.getName());
            selected_contentclass = null;
            selected_datalisttclass = null;
            selected_asset = null;
            selected_assetlist = null;
            selected_keywordlist = null;
            current_classcontent = null;
            current_classcontentlist = null;
            current_asset = null;
            current_assetlibrary = null;
            current_keywordlibrary = null;
            FacesMessage message = new FacesMessage("DIV selected " + selectedDiv.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void invertShow(CfDiv div) {
        if (null != div) {
            div.setVisible(!div.isVisible());
        }
    }
    
    public void onChangeLayoutContent() {
        if (null != selected_contentclass) {
            current_layoutcontent = null;
            showContent = true;
            showDatalist = false;
            showAsset = false;
            showAssetLibrary = false;
            showKeywordLibrary = false;
            String[] contentinfos = selected_contentclass.split(":");
            String contentclass = contentinfos[0];
            CfClass selectedclass = cfclassService.findByName(contentclass);
            int lfdnr = Integer.parseInt(contentinfos[1]);
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttype(selectedSite.getId(), selectedDivTemplate.getId(), "C");
            long classcontentref = 0;
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                if (layoutcontent.getCfLayoutcontentPK().getLfdnr() == lfdnr) {
                    classcontentref = layoutcontent.getPreview_contentref().longValue();
                    current_layoutcontent = layoutcontent;
                }
            }
            if (0 != classcontentref) {
                current_classcontent = cfclasscontentService.findById(classcontentref);
            }
            current_classcontentlist = cfclasscontentService.findByClassref(selectedclass);
            
            selected_datalisttclass = null;
            selected_asset = null;
            selected_assetlist = null;
            selected_keywordlist = null;
        }
    }
    
    public void onSaveLayoutContent(CfClasscontent classcontent) {
        if (null != classcontent) {
            String[] contentinfos = selected_contentclass.split(":");
            int lfdnr = Integer.parseInt(contentinfos[1]);
            current_layoutcontent = new CfLayoutcontent(selectedSite.getId(), selectedDivTemplate.getId(), "C", lfdnr);
            current_layoutcontent.setContentref(BigInteger.ZERO);          
            current_layoutcontent.setPreview_contentref(BigInteger.valueOf(classcontent.getId()));
            try {
                cflayoutcontentService.create(current_layoutcontent);
            } catch (Exception ex) {
                cflayoutcontentService.edit(current_layoutcontent);
            }
        }
    }
    
    /**
     * Selects a Content
     * @param event
     */
    public void onSelectLayoutContent(SelectEvent event) {
        current_classcontent = (CfClasscontent) event.getObject();
        if (null != current_classcontent) {
            attributcontentlist = cfattributcontentService.findByClasscontentref(current_classcontent);
            String output = current_classcontent.getClassref().getTemplateref().getContent();
            for (CfAttributcontent attributcontent : attributcontentlist) {
                output = output.replaceAll("#" + attributcontent.getAttributref().getName() + "#", attributcontent.toString());
            }
            previewContentOutput = output;
        }
    }
    
    public void onChangeLayoutDatalist() {
        if (null != selected_datalisttclass) {
            current_layoutcontent = null;
            showContent = false;
            showDatalist = true;
            showAsset = false;
            showAssetLibrary = false;
            showKeywordLibrary = false;
            String[] datalistinfos = selected_datalisttclass.split(":");
            String list = datalistinfos[0];
            CfClass selectedclass = cfclassService.findByName(list);
            int lfdnr = Integer.parseInt(datalistinfos[1]);
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttype(selectedSite.getId(), selectedDivTemplate.getId(), "DL");
            long listref = 0;
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                if (layoutcontent.getCfLayoutcontentPK().getLfdnr() == lfdnr) {
                    listref = layoutcontent.getPreview_contentref().longValue();
                    current_layoutcontent = layoutcontent;
                }
            }
            if (0 != listref) {
                current_list = cflistService.findById(listref);
            }
            current_datalist = cflistService.findByClassref(selectedclass);
            
            selected_contentclass = null;
            selected_asset = null;
            selected_assetlist = null;
            selected_keywordlist = null;
        }
    }
    
    /**
     * Selects a Datalist
     * @param event
     */
    public void onSelectLayoutDatalist(SelectEvent event) {
        CfList selected_datalist = (CfList) event.getObject();
        
        previewDatalistOutput = "";
        for (CfListcontent datalistcontent : cflistcontentService.findByListref(selected_datalist.getId())) {
            
            CfClasscontent cc = cfclasscontentService.findById(datalistcontent.getCfListcontentPK().getClasscontentref());
            String template = templateUtility.getVersion(cc.getClassref().getTemplateref().getId(), cftemplateversionService.findMaxVersion(cc.getClassref().getTemplateref().getId()));
            
            if (null != cc) {
                attributcontentlist = cfattributcontentService.findByClasscontentref(cc);
                //String output = cc.getClassref().getTemplateref().getContent();
                for (CfAttributcontent attributcontent : attributcontentlist) {
                    template = template.replaceAll("#" + attributcontent.getAttributref().getName() + "#", attributcontent.toString());
                }
                previewDatalistOutput += template;
            }
        }
    }
    
    public void onSaveLayoutDatalist(CfList datalist) {
        if (null != datalist) {
            if (null == current_layoutcontent) {
                String[] datalistinfos = selected_datalisttclass.split(":");
                int lfdnr = Integer.parseInt(datalistinfos[1]);
                current_layoutcontent = new CfLayoutcontent(selectedSite.getId(), selectedDivTemplate.getId(), "DL", lfdnr);
                current_layoutcontent.setContentref(BigInteger.ZERO);
            }
            current_layoutcontent.setPreview_contentref(BigInteger.valueOf(datalist.getId()));
            try {
                cflayoutcontentService.create(current_layoutcontent);
            } catch (Exception ex) {
                cflayoutcontentService.edit(current_layoutcontent);
            }
        }
    }
    
    public void onChangeLayoutAsset() {
        if (null != selected_asset) {
            current_layoutcontent = null;
            showContent = false;
            showDatalist = false;
            showAsset = true;
            showAssetLibrary = false;
            showKeywordLibrary = false;
            String[] assetinfos = selected_asset.split(":");
            String name = assetinfos[0];
            int lfdnr = Integer.parseInt(assetinfos[1]);
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttype(selectedSite.getId(), selectedDivTemplate.getId(), "A");
            long assetref = 0;
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                if (layoutcontent.getCfLayoutcontentPK().getLfdnr() == lfdnr) {
                    assetref = layoutcontent.getPreview_contentref().longValue();
                    current_layoutcontent = layoutcontent;
                }
            }
            if (0 != assetref) {
                current_asset = cfassetService.findById(assetref);
            }
            current_assetlist = cfassetService.findByScrapped(false);
            
            selected_contentclass = null;
            selected_datalisttclass = null;
            selected_assetlist = null;
            selected_keywordlist = null;
        }
    }
    
    public void onSaveLayoutAsset(CfAsset asset) {
        if (null != asset) {
            if (null == current_layoutcontent) {
                String[] assetinfos = selected_asset.split(":");
                int lfdnr = Integer.parseInt(assetinfos[1]);
                current_layoutcontent = new CfLayoutcontent(selectedSite.getId(), selectedDivTemplate.getId(), "A", lfdnr);
                current_layoutcontent.setContentref(BigInteger.ZERO);
            }
            current_layoutcontent.setPreview_contentref(BigInteger.valueOf(asset.getId()));
            try {
                cflayoutcontentService.create(current_layoutcontent);
            } catch (Exception ex) {
                cflayoutcontentService.edit(current_layoutcontent);
            }
        }
    }
    
    /**
     * Selects a Asset
     * @param event
     */
    public void onSelectLayoutAsset(SelectEvent event) {
        current_asset = (CfAsset) event.getObject();
        if (null != current_asset) {
            previewAssetOutput = current_asset.getId();
        }
    }
    
    public void onChangeLayoutAssetlibrary() {
        if (null != selected_assetlist) {
            current_layoutcontent = null;
            showContent = false;
            showDatalist = false;
            showAsset = false;
            showAssetLibrary = true;
            showKeywordLibrary = false;
            String[] assetlibraryinfos = selected_assetlist.split(":");
            String assetlist = assetlibraryinfos[0];
            int lfdnr = Integer.parseInt(assetlibraryinfos[1]);
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttype(selectedSite.getId(), selectedDivTemplate.getId(), "AL");
            long listref = 0;
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                if (layoutcontent.getCfLayoutcontentPK().getLfdnr() == lfdnr) {
                    listref = layoutcontent.getPreview_contentref().longValue();
                    current_layoutcontent = layoutcontent;
                }
            }
            if (0 != listref) {
                current_assetlibrary = cfassetlistService.findById(listref);
            }
            current_assetlibrarylist = cfassetlistService.findAll();
            
            selected_contentclass = null;
            selected_asset = null;
            selected_datalisttclass = null;
            selected_keywordlist = null;
        }
    }
    
    /**
     * Selects an Assetlibrary
     * @param event
     */
    public void onSelectLayoutAssetlibrary(SelectEvent event) {
        CfAssetlist selected_assetlist = (CfAssetlist) event.getObject();
        
        previewAssetlistOutput.clear();
        for (CfAssetlistcontent assetlistcontent : cfassetlistcontentService.findByAssetlistref(selected_assetlist.getId())) {
            previewAssetlistOutput.add(cfassetService.findById(assetlistcontent.getCfAssetlistcontentPK().getAssetref()));
        }
    }
    
    public void onSaveLayoutAssetlist(CfAssetlist assetlist) {
        if (null != assetlist) {
            if (null == current_layoutcontent) {
                String[] assetlistinfos = selected_assetlist.split(":");
                int lfdnr = Integer.parseInt(assetlistinfos[1]);
                current_layoutcontent = new CfLayoutcontent(selectedSite.getId(), selectedDivTemplate.getId(), "AL", lfdnr);
                current_layoutcontent.setContentref(BigInteger.ZERO);
            }
            current_layoutcontent.setPreview_contentref(BigInteger.valueOf(assetlist.getId()));
            try {
                cflayoutcontentService.create(current_layoutcontent);
            } catch (Exception ex) {
                cflayoutcontentService.edit(current_layoutcontent);
            }
        }
    }
    
    public void onChangeLayoutKeywordlibrary() {
        if (null != selected_keywordlist) {
            current_layoutcontent = null;
            showContent = false;
            showDatalist = false;
            showAsset = false;
            showAssetLibrary = false;
            showKeywordLibrary = true;
            String[] keywordlibraryinfos = selected_keywordlist.split(":");
            String keywordlist = keywordlibraryinfos[0];
            int lfdnr = Integer.parseInt(keywordlibraryinfos[1]);
            List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttype(selectedSite.getId(), selectedDivTemplate.getId(), "KL");
            long listref = 0;
            for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                if (layoutcontent.getCfLayoutcontentPK().getLfdnr() == lfdnr) {
                    listref = layoutcontent.getPreview_contentref().longValue();
                    current_layoutcontent = layoutcontent;
                }
            }
            if (0 != listref) {
                current_keywordlibrary = cfkeywordlistService.findById(listref);
            }
            current_keywordlibrarylist = cfkeywordlistService.findAll();
            
            selected_contentclass = null;
            selected_asset = null;
            selected_datalisttclass = null;
            selected_assetlist = null;
        }
    }
    
    public void onSaveLayoutKeywordlist(CfKeywordlist keywordlist) {
        if (null != keywordlist) {
            if (null != current_keywordlibrary) {
                String[] keywordlistinfos = selected_keywordlist.split(":");
                int lfdnr = Integer.parseInt(keywordlistinfos[1]);
                current_layoutcontent = new CfLayoutcontent(selectedSite.getId(), selectedDivTemplate.getId(), "KL", lfdnr);
                current_layoutcontent.setContentref(BigInteger.ZERO);
            }
            current_layoutcontent.setPreview_contentref(BigInteger.valueOf(keywordlist.getId()));
            try {
                cflayoutcontentService.create(current_layoutcontent);
            } catch (Exception ex) {
                cflayoutcontentService.edit(current_layoutcontent);
            }
        }
    }
    
    /**
     * Selects a Keywordlibrary
     * @param event
     */
    public void onSelectLayoutKeywordlibrary(SelectEvent event) {
        CfKeywordlist selected_keywordlist = (CfKeywordlist) event.getObject();
        
        previewKeywordlistOutput.clear();
        for (CfKeywordlistcontent keywordlistcontent : cfkeywordlistcontentService.findByKeywordlistref(selected_keywordlist.getId())) {
            previewKeywordlistOutput.add(cfkeywordService.findById(keywordlistcontent.getCfKeywordlistcontentPK().getKeywordref()));
        }
    }
    
    public void onGenerateShorturl() {
        if (null != selectedSite) {
            selectedSite.setShorturl(siteUtil.generateShorturl());
            cfsiteService.edit(selectedSite);
            loadTree();
            FacesMessage message = new FacesMessage("Generated shorturl for " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}
