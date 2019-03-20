package io.clownfish.clownfish.beans;

import KNSAPTools.SAPConnection;
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
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Named("sitetree")
@ManagedBean(name = "sitetree")
public class SiteTreeBean implements Serializable {
    public static final String SAPCONNECTION = "sapconnection.props";
    private static SAPConnection sapc = null;
    
    private @Getter @Setter TreeNode root;
    private @Getter @Setter TreeNode selectedNode = null;
    private @Getter @Setter String siteName;
    private @Getter @Setter CfSite selectedSite = null;
    private @Getter @Setter CfTemplate selectedTemplate = null;
    private @Getter @Setter CfStylesheet selectedStylesheet = null;
    private @Getter @Setter CfJavascript selectedJavascript = null;
    private @Getter @Setter List<CfTemplate> templatelist;
    private @Getter @Setter List<CfStylesheet> stylesheetlist;
    private @Getter @Setter List<CfJavascript> javascriptlist;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter List<CfDatasource> datasources;
    private @Getter @Setter List<CfDatasource> selectedDatasources;
    private @Getter @Setter List<CfList> contentlist;
    private @Getter @Setter List<CfList> selectedContentlist;
    private @Getter @Setter List<CfSitesaprfc> saprfclist = null;
    private @Getter @Setter CfSitesaprfc selectedrfc = null;
    private @Getter @Setter List<RfcGroup> rfcgrouplist;
    private @Getter @Setter RfcGroup selectedrfcgroup = null;
    private @Getter @Setter List<RfcFunction> rfcfunctionlist;
    private @Getter @Setter RfcFunction selectedrfcfunction = null;
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter List<CfClasscontent> selectedclasscontentlist;
    private @Getter @Setter int sitehtmlcompression;
    private @Getter @Setter String aliaspath;
    private @Getter @Setter String characterEncoding;
    private @Getter @Setter String contentType;
    private @Getter @Setter String locale;
    private @Getter @Setter Map<String, String> propertymap = null;
    private @Getter @Setter boolean sapSupport = false;
    
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfSiteService cfsiteService;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfSitecontentService cfsitecontentService;
    @Autowired CfListService cflistService;
    @Autowired CfSitelistService cfsitelistService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    @Autowired CfPropertyService cfpropertyService;
    @Autowired PropertyList propertylist;
    
    @PostConstruct
    public void init() {
        //propertymap = new PropertyList().init(em);
        propertymap = propertylist.fillPropertyMap();
        String sapSupportProp = propertymap.get("sap.support");
        if (sapSupportProp == null) {
            sapSupport = false;
        } else {
            if (sapSupportProp.compareToIgnoreCase("TRUE") == 0) {
                sapSupport = true;
            }
            if (sapSupport) {
                sapc = new SAPConnection(SAPCONNECTION, "Gemini4");
                rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
            }
        }
        root = new DefaultTreeNode("Root", null);
        loadTree();
        templatelist =  cftemplateService.findAll();
        stylesheetlist = cfstylesheetService.findAll();
        javascriptlist = cfjavascriptService.findAll();
        //datasources = em.createNamedQuery("Kndatasource.findAll").getResultList();
        datasources = cfdatasourceService.findAll();
        //contentlist = em.createNamedQuery("Knlist.findAll").getResultList();
        contentlist = cflistService.findAll();
        //classcontentlist = em.createNamedQuery("Knclasscontent.findAll").getResultList();
        classcontentlist = cfclasscontentService.findAll();
        
        selectedDatasources = new ArrayList<>();
        selectedContentlist = new ArrayList<>();
        selectedclasscontentlist = new ArrayList<>();
        locale = propertymap.get("response.locale");
        contentType = propertymap.get("response.contenttype");
        characterEncoding = propertymap.get("response.characterencoding");
    }
    
    public void initTemplatelist() {
        //templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
        templatelist =  cftemplateService.findAll();
    }
    
    public void initStylesheetlist() {
        //stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
        stylesheetlist = cfstylesheetService.findAll();
    }
    
    public void initJavascriptlist() {
        //javascriptlist = em.createNamedQuery("Knjavascript.findAll").getResultList();
        javascriptlist = cfjavascriptService.findAll();
    }
    
    public void initDatasources() {
        //datasources = em.createNamedQuery("Kndatasource.findAll").getResultList();
        datasources = cfdatasourceService.findAll();
    }
    
    public void initContentlist() {
        //contentlist = em.createNamedQuery("Knlist.findAll").getResultList();
        contentlist = cflistService.findAll();
    }

    private void fillChildren(long parentid, TreeNode node) {
        //List<CfSite> sitelist = em.createNamedQuery("Knsite.findByParentref").setParameter("parentref", parentid).getResultList();
        List<CfSite> sitelist = cfsiteService.findByParentref(parentid);
                
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            node.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
    
    private void loadTree() {
        //List<CfSite> sitelist = em.createNamedQuery("Knsite.findByParentref").setParameter("parentref", 0).getResultList();
        List<CfSite> sitelist = cfsiteService.findByParentref(0L);
        
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            root.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
 
    public String getTemplate() {
        if (selectedSite != null) {
            if (selectedTemplate != null) {
                return selectedTemplate.getContent();
            } else {
                try {
                    //CfTemplate template = (CfTemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", selectedSite.getTemplateref()).getSingleResult();
                    CfTemplate template = cftemplateService.findById(selectedSite.getTemplateref().longValue());
                    return template.getContent();
                } catch (NoResultException ex) {
                    return "";
                }    
            }
        } else {
            return "";
        }
    }
    
    public String getStylesheet() {
        if (selectedSite != null) {
            if (selectedStylesheet != null) {
                return selectedStylesheet.getContent();
            } else {
                try {
                    //CfStylesheet stylesheet = (CfStylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", selectedSite.getStylesheetref()).getSingleResult();
                    CfStylesheet stylesheet = cfstylesheetService.findById(selectedSite.getStylesheetref().longValue());
                    return stylesheet.getContent();
                } catch (NoResultException ex) {
                    return "";
                }
            }
        } else {
            return "";
        }
    }
    
    public String getJavascript() {
        if (selectedSite != null) {
            if (selectedJavascript != null) {
                return selectedJavascript.getContent();
            } else {
                try {
                    //Knjavascript javascript = (Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", selectedSite.getJavascriptref()).getSingleResult();
                    CfJavascript javascript = cfjavascriptService.findById(selectedSite.getJavascriptref().longValue());
                    return javascript.getContent();
                } catch (NoResultException ex) {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public void onDragDrop(TreeDragDropEvent event) {
        TreeNode dragNode = event.getDragNode();
        TreeNode dropNode = event.getDropNode();
        int dropIndex = event.getDropIndex();
        
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Dragged " + dragNode.getData(), "Dropped on " + dropNode.getData() + " at " + dropIndex);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onSelect(NodeSelectEvent event) {
        selectedNode = event.getTreeNode();
        selectedSite = (CfSite) selectedNode.getData();
        if (selectedSite.getTemplateref() != null) {
            //Kntemplate template = (Kntemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", selectedSite.getTemplateref()).getSingleResult();
            CfTemplate template = cftemplateService.findById(selectedSite.getTemplateref().longValue());
            int idx = templatelist.indexOf(template);
            selectedTemplate = templatelist.get(idx);
        } else {
            selectedTemplate = null;
        }
        if (selectedSite.getStylesheetref() != null) {
            //Knstylesheet styleshet = (Knstylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", selectedSite.getStylesheetref()).getSingleResult();
            CfStylesheet styleshet = cfstylesheetService.findById(selectedSite.getStylesheetref().longValue());
            int idx = stylesheetlist.indexOf(styleshet);
            selectedStylesheet = stylesheetlist.get(idx);
        } else {
            selectedStylesheet = null;
        }
        if (selectedSite.getJavascriptref() != null) {
            //Knjavascript javascript = (Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", selectedSite.getJavascriptref()).getSingleResult();
            CfJavascript javascript = cfjavascriptService.findById(selectedSite.getJavascriptref().longValue());
            int idx = javascriptlist.indexOf(javascript);
            selectedJavascript = javascriptlist.get(idx);
        } else {
            selectedJavascript = null;
        }
        selectedDatasources.clear();
        //List<CfSitedatasource> selectedSiteDatasources = em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        List<CfSitedatasource> selectedSiteDatasources = cfsitedatasourceService.findBySiteref(selectedSite.getId());
        for (CfSitedatasource sitedatasource : selectedSiteDatasources) {
            //CfDatasource ds = (CfDatasource) em.createNamedQuery("Kndatasource.findById").setParameter("id", sitedatasource.getKnsitedatasourcePK().getDatasourceref()).getSingleResult();
            CfDatasource ds = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
            selectedDatasources.add(ds);
        }
        
        selectedContentlist.clear();
        //List<Knsitelist> selectedSitecontentlist = em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        List<CfSitelist> selectedSitecontentlist = cfsitelistService.findBySiteref(selectedSite.getId());
        for (CfSitelist sitelist : selectedSitecontentlist) {
            //Knlist cl = (Knlist) em.createNamedQuery("Knlist.findById").setParameter("id", sitelist.getKnsitelistPK().getListref()).getSingleResult();
            CfList cl = cflistService.findById(sitelist.getCfSitelistPK().getListref());
            selectedContentlist.add(cl);
        }
        
        selectedclasscontentlist.clear();
        //List<CfSitecontent> selectedClasscontentlist = em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        List<CfSitecontent> selectedClasscontentlist = cfsitecontentService.findBySiteref(selectedSite.getId());
        for (CfSitecontent sitecontent : selectedClasscontentlist) {
            //CfClasscontent cc = (Knclasscontent) em.createNamedQuery("Knclasscontent.findById").setParameter("id", sitecontent.getKnsitecontentPK().getClasscontentref()).getSingleResult();
            CfClasscontent cc = cfclasscontentService.findById(sitecontent.getCfSitecontentPK().getClasscontentref());
            selectedclasscontentlist.add(cc);
        }
        
        siteName = selectedSite.getName();
        aliaspath = selectedSite.getAliaspath();
        sitehtmlcompression = selectedSite.getHtmlcompression();
        characterEncoding = selectedSite.getCharacterencoding();
        contentType = selectedSite.getContenttype();
        locale = selectedSite.getLocale();
        
        //saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        saprfclist = cfsitesaprfcService.findBySiteref(selectedSite.getId());
        
        newButtonDisabled = true;
        
        FacesMessage message = new FacesMessage("Selected " + selectedSite.getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (selectedSite != null) {
            //knsiteFacadeREST.remove(selectedSite);
            cfsiteService.delete(selectedSite);
            loadTree();
            
            FacesMessage message = new FacesMessage("Deleted " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChange(ActionEvent actionEvent) {
        if (selectedSite != null) {
            if (selectedStylesheet != null) {
                selectedSite.setStylesheetref(BigInteger.valueOf(selectedStylesheet.getId().intValue()));
            } else {
                selectedSite.setStylesheetref(null);
            }
            if (selectedTemplate != null) {
                selectedSite.setTemplateref(BigInteger.valueOf(selectedTemplate.getId().intValue()));
            } else {
                selectedSite.setTemplateref(null);
            }
            if (selectedJavascript != null) {
                selectedSite.setJavascriptref(BigInteger.valueOf(selectedJavascript.getId().intValue()));
            } else {
                selectedSite.setJavascriptref(null);
            }
            
            // Delete siteresources first
            //List<Knsitedatasource> sitedatasourceList = em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            List<CfSitedatasource> sitedatasourceList = cfsitedatasourceService.findBySiteref(selectedSite.getId());
            for (CfSitedatasource sitedatasource : sitedatasourceList) {
                cfsitedatasourceService.delete(sitedatasource);
                //knsitedatasourceFacadeREST.remove(sitedatasource);
            }
            // Add selected siteresources
            if (selectedDatasources.size() > 0) {
                //for (int i = 0; i <= selectedDatasources.size(); i++) {
                for (CfDatasource datasource : selectedDatasources) {
                    CfSitedatasource sitedatasource = new CfSitedatasource();
                    CfSitedatasourcePK cfsitedatasourcePK = new CfSitedatasourcePK();
                    cfsitedatasourcePK.setSiteref(selectedSite.getId());
                    cfsitedatasourcePK.setDatasourceref(datasource.getId());
                    sitedatasource.setCfSitedatasourcePK(cfsitedatasourcePK);
                    cfsitedatasourceService.create(sitedatasource);
                    //cfsitedatasourceFacadeREST.create(sitedatasource);
                }
            }
            
            // Delete sitelists first
            //List<CfSitelist> sitelists = em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            List<CfSitelist> sitelists = cfsitelistService.findBySiteref(selectedSite.getId());
            for (CfSitelist sitelist : sitelists) {
                //CfSitelistFacadeREST.remove(sitelist);
                cfsitelistService.delete(sitelist);
            }
            // Add selected sitelists
            if (selectedContentlist.size() > 0) {
                //for (int i = 0; i <= selectedDatasources.size(); i++) {
                for (CfList contentlist : selectedContentlist) {
                    CfSitelist sitelist = new CfSitelist();
                    CfSitelistPK knsitelistPK = new CfSitelistPK();
                    knsitelistPK.setSiteref(selectedSite.getId());
                    knsitelistPK.setListref(contentlist.getId());
                    sitelist.setCfSitelistPK(knsitelistPK);
                    //knsitelistFacadeREST.create(sitelist);
                    cfsitelistService.create(sitelist);
                }
            }
            
            // Delete sitecontent first
            //List<CfSitecontent> contentlists = em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            List<CfSitecontent> contentlists = cfsitecontentService.findBySiteref(selectedSite.getId());
            for (CfSitecontent content : contentlists) {
                //knsitecontentFacadeREST.remove(content);
                cfsitecontentService.delete(content);
            }
            // Add selected sitecontent
            if (selectedclasscontentlist.size() > 0) {
                //for (int i = 0; i <= selectedDatasources.size(); i++) {
                for (CfClasscontent content : selectedclasscontentlist) {
                    CfSitecontent sitecontent = new CfSitecontent();
                    CfSitecontentPK knsitecontentPK = new CfSitecontentPK();
                    knsitecontentPK.setSiteref(selectedSite.getId());
                    knsitecontentPK.setClasscontentref(content.getId());
                    sitecontent.setCfSitecontentPK(knsitecontentPK);
                    //knsitecontentFacadeREST.create(sitecontent);
                    cfsitecontentService.create(sitecontent);
                }
            }
            selectedSite.setHtmlcompression(sitehtmlcompression);
            selectedSite.setCharacterencoding(characterEncoding);
            selectedSite.setContenttype(contentType);
            selectedSite.setLocale(locale);
            selectedSite.setAliaspath(aliaspath);
            //knsiteFacadeREST.edit(selectedSite);
            cfsiteService.edit(selectedSite);
            loadTree();
            
            FacesMessage message = new FacesMessage("Changed " + selectedSite.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChangeTemplate() {
        if (selectedTemplate != null) {
            
        }
    }
    
    public void onChangeStylesheet() {
        if (selectedStylesheet != null) {
            
        }
    }
    
    public void onChangeJavascript() {
        if (selectedStylesheet != null) {
            
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            //CfSite validateSite = (CfSite) em.createNamedQuery("Knsite.findByName").setParameter("name", siteName).getSingleResult();
            CfSite validateSite = cfsiteService.findByName(siteName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = siteName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfSite newsite = new CfSite();
            newsite.setName(siteName);
            if (selectedSite != null) {
                newsite.setParentref(BigInteger.valueOf(selectedSite.getId()));
            } else {
                newsite.setParentref(BigInteger.ZERO);
            }
            if (selectedTemplate != null) {
                newsite.setTemplateref(BigInteger.valueOf(selectedTemplate.getId()));
            }
            if (selectedStylesheet != null) {
                newsite.setStylesheetref(BigInteger.valueOf(selectedStylesheet.getId()));
            }
            if (selectedJavascript != null) {
                newsite.setJavascriptref(BigInteger.valueOf(selectedJavascript.getId()));
            }
            newsite.setHtmlcompression(sitehtmlcompression);
            newsite.setContenttype(contentType);
            newsite.setCharacterencoding(characterEncoding);
            newsite.setLocale(locale);
            newsite.setAliaspath(siteName);
            //knsiteFacadeREST.create(newsite);
            cfsiteService.create(newsite);
            loadTree();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onChangeRfcGroup() {
        if (selectedrfcgroup != null) {
            rfcfunctionlist = new RFC_FUNCTION_SEARCH(sapc).getRfcFunctionsList(selectedrfcgroup.getName());
        }
    }
    
    public void onChangeRfcFunction() {
        if (selectedrfcfunction != null) {
            
        }
    }
    
    public void onDeleteRfc(ActionEvent actionEvent) {
        if (selectedrfc != null) {
            //knsitesaprfcFacadeREST.remove(selectedrfc);
            cfsitesaprfcService.delete(selectedrfc);
            
            //saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            saprfclist = cfsitesaprfcService.findBySiteref(selectedSite.getId());
        }
    }
    
    public void onNewRfc(ActionEvent actionEvent) {
        if ((selectedrfcgroup != null) && (selectedrfcfunction != null)) {
            CfSitesaprfc sitesaprfc = new CfSitesaprfc();
            CfSitesaprfcPK cfsitesaprfcPK = new CfSitesaprfcPK();
            cfsitesaprfcPK.setSiteref(selectedSite.getId());
            cfsitesaprfcPK.setRfcgroup(selectedrfcgroup.getName());
            cfsitesaprfcPK.setRfcfunction(selectedrfcfunction.getName());
            sitesaprfc.setCfSitesaprfcPK(cfsitesaprfcPK);
            //cfsitesaprfcFacadeREST.create(sitesaprfc);
            cfsitesaprfcService.create(sitesaprfc);
            
            //saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
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
            rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
            for (RfcGroup rfcgroup : rfcgrouplist) {
                if (rfcgroup.getName().compareToIgnoreCase(value) == 0 ) {
                    return rfcgroup;
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
