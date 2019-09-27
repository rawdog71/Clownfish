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
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitecontentPK;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasourcePK;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.dbentities.CfSitelistPK;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfSitesaprfcPK;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.models.RfcFunction;
import io.clownfish.clownfish.sap.models.RfcGroup;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfPropertyService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.JavascriptUtil;
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
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author sulzbachr
 */
@Scope("session")
@Named("sitetree")
public class SiteTreeBean implements Serializable {
    public static final String SAPCONNECTION = "sapconnection.props";
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
    private @Getter @Setter RfcGroup selectedrfcgroup = null;
    private @Getter @Setter List<RfcFunction> rfcfunctionlist;
    private @Getter @Setter RfcFunction selectedrfcfunction = null;
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter List<CfClasscontent> selectedClasscontentlist;
    private @Getter @Setter int sitehtmlcompression;
    private @Getter @Setter int sitegzip;
    private @Getter @Setter boolean sitejob;
    private @Getter @Setter boolean sitestatic;
    private @Getter @Setter boolean sitesearchrelevant;
    private @Getter @Setter String siteTitle;
    private @Getter @Setter String siteDescription;
    private @Getter @Setter String aliaspath;
    private @Getter @Setter String characterEncoding;
    private @Getter @Setter String contentType;
    private @Getter @Setter String locale;
    private transient @Getter @Setter Map<String, String> propertymap = null;
    private @Getter @Setter boolean sapSupport = false;
    
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired transient CfSiteService cfsiteService;
    @Autowired transient CfDatasourceService cfdatasourceService;
    @Autowired transient CfSitedatasourceService cfsitedatasourceService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfSitelistService cfsitelistService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfSitesaprfcService cfsitesaprfcService;
    @Autowired transient CfPropertyService cfpropertyService;
    @Autowired transient PropertyList propertylist;
    @Autowired transient TemplateList templatelist;
    @Autowired transient StylesheetList stylesheetlist;
    @Autowired transient JavascriptList javascriptlist;
    @Autowired private @Getter @Setter TemplateUtil templateUtility;
    @Autowired private @Getter @Setter StylesheetUtil stylesheetUtility;
    @Autowired private @Getter @Setter JavascriptUtil javascriptUtility;
    @Autowired transient FolderUtil folderUtil;
    
    final transient Logger logger = LoggerFactory.getLogger(SiteTreeBean.class);
    
    @PostConstruct
    public void init() {
        propertymap = propertylist.fillPropertyMap();
        String sapSupportProp = propertymap.get("sap_support");
        if (sapSupportProp == null) {
            sapSupport = false;
        } else {
            if (sapSupportProp.compareToIgnoreCase("TRUE") == 0) {
                sapSupport = true;
            }
            if (sapSupport) {
                sapc = new SAPConnection(SAPCONNECTION, "Clownfish4");
                rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
            }
        }
        //root = new DefaultTreeNode("Root", null);
        loadTree();
        datasources = cfdatasourceService.findAll();
        contentlist = cflistService.findAll();
        classcontentlist = cfclasscontentService.findAll();
        
        selectedDatasources = new ArrayList<>();
        selectedContentlist = new ArrayList<>();
        selectedClasscontentlist = new ArrayList<>();
        locale = propertymap.get("response_locale");
        contentType = propertymap.get("response_contenttype");
        characterEncoding = propertymap.get("response_characterencoding");
    }
    
    public void initDatasources() {
        datasources = cfdatasourceService.findAll();
    }
    
    public void initContentlist() {
        contentlist = cflistService.findAll();
    }
    
    public void initClassContentlist() {
        classcontentlist = cfclasscontentService.findAll();
    }

    private void fillChildren(long parentid, TreeNode node) {
        List<CfSite> sitelist = cfsiteService.findByParentref(parentid);
                
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            node.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
    
    private void loadTree() {
        root = new DefaultTreeNode("Root", null);
        List<CfSite> sitelist = cfsiteService.findByParentref(0L);
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            root.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
 
    public String getTemplate() {
        if (null != selectedSite) {
            if (null != selectedTemplate) {
                templateUtility.setTemplateContent(selectedTemplate.getContent());
                return templateUtility.getTemplateContent();
            } else {
                try {
                    CfTemplate template = cftemplateService.findById(selectedSite.getTemplateref().longValue());
                    return template.getContent();
                } catch (Exception ex) {
                    return "";
                }    
            }
        } else {
            return "";
        }
    }
    
    public String getStylesheet() {
        if (null != selectedSite) {
            if (null != selectedStylesheet) {
                stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
                return stylesheetUtility.getStyelsheetContent();
            } else {
                try {
                    CfStylesheet stylesheet = cfstylesheetService.findById(selectedSite.getStylesheetref().longValue());
                    return stylesheet.getContent();
                } catch (Exception ex) {
                    return "";
                }
            }
        } else {
            return "";
        }
    }
    
    public String getJavascript() {
        if (null != selectedSite) {
            if (null != selectedJavascript) {
                javascriptUtility.setJavascriptContent(selectedJavascript.getContent());
                return javascriptUtility.getJavascriptContent();
            } else {
                try {
                    CfJavascript javascript = cfjavascriptService.findById(selectedSite.getJavascriptref().longValue());
                    return javascript.getContent();
                } catch (Exception ex) {
                    return "";
                }
            }
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
        sitejob = false;
        sitesearchrelevant = false;
        sitestatic = false;
        newButtonDisabled = false;
    }
    
    public void onSelect(NodeSelectEvent event) {
        selectedNode = event.getTreeNode();
        selectedSite = (CfSite) selectedNode.getData();
        if (null != selectedSite.getTemplateref()) {
            CfTemplate template = cftemplateService.findById(selectedSite.getTemplateref().longValue());
            int idx = templatelist.getTemplateListe().indexOf(template);
            selectedTemplate = templatelist.getTemplateListe().get(idx);
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
        siteName = selectedSite.getName();
        siteTitle = selectedSite.getTitle();
        siteDescription = selectedSite.getDescription();
        sitejob = selectedSite.isJob();
        sitesearchrelevant = selectedSite.isSearchrelevant();
        sitestatic = selectedSite.isStaticsite();
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
            if (selectedDatasources.size() > 0) {
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
            if (selectedContentlist.size() > 0) {
                for (CfList contentlist : selectedContentlist) {
                    CfSitelist sitelist = new CfSitelist();
                    CfSitelistPK knsitelistPK = new CfSitelistPK();
                    knsitelistPK.setSiteref(selectedSite.getId());
                    knsitelistPK.setListref(contentlist.getId());
                    sitelist.setCfSitelistPK(knsitelistPK);
                    cfsitelistService.create(sitelist);
                }
            }
            
            // Delete sitecontent first
            List<CfSitecontent> contentlists = cfsitecontentService.findBySiteref(selectedSite.getId());
            for (CfSitecontent content : contentlists) {
                cfsitecontentService.delete(content);
            }
            // Add selected sitecontent
            if (selectedClasscontentlist.size() > 0) {
                for (CfClasscontent content : selectedClasscontentlist) {
                    CfSitecontent sitecontent = new CfSitecontent();
                    CfSitecontentPK knsitecontentPK = new CfSitecontentPK();
                    knsitecontentPK.setSiteref(selectedSite.getId());
                    knsitecontentPK.setClasscontentref(content.getId());
                    sitecontent.setCfSitecontentPK(knsitecontentPK);
                    cfsitecontentService.create(sitecontent);
                }
            }
            selectedSite.setHtmlcompression(sitehtmlcompression);
            selectedSite.setCharacterencoding(characterEncoding);
            selectedSite.setContenttype(contentType);
            selectedSite.setLocale(locale);
            selectedSite.setAliaspath(aliaspath);
            selectedSite.setTitle(siteTitle);
            selectedSite.setDescription(siteDescription);
            selectedSite.setJob(sitejob);
            selectedSite.setSearchrelevant(sitesearchrelevant);
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
            newsite.setStaticsite(sitestatic);
            cfsiteService.create(newsite);
            loadTree();
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    public void onDeleteStaticSite(ActionEvent actionEvent) {
        if (null != folderUtil.getStatic_folder()) {
            File file = new File(folderUtil.getStatic_folder() + File.separator +  selectedSite.getName());
            try {
                Files.deleteIfExists(file.toPath());
                FacesMessage message = new FacesMessage("Deleted static site for " + selectedSite.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    public void onChangeRfcGroup() {
        if (null != selectedrfcgroup) {
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
                for (RfcGroup rfcgroup : rfcgrouplist) {
                    if (rfcgroup.getName().compareToIgnoreCase(value) == 0 ) {
                        return rfcgroup;
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
}
