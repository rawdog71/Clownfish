package io.clownfish.clownfish.beans;

import KNSAPTools.SAPConnection;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitecontentPK;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.dbentities.CfSitelistPK;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.models.RfcFunction;
import io.clownfish.clownfish.sap.models.RfcGroup;
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
    
    @PostConstruct
    public void init() {
        //propertymap = new PropertyList().init(em);
        propertymap = new PropertyList().fillPropertyMap();
        String sapSupportProp = propertymap.get("sap.support");
        if (sapSupportProp.compareToIgnoreCase("TRUE") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            sapc = new SAPConnection(SAPCONNECTION, "Gemini4");
            rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
        }
        root = new DefaultTreeNode("Root", null);
        loadTree();
        stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
        templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
        javascriptlist = em.createNamedQuery("Knjavascript.findAll").getResultList();
        datasources = em.createNamedQuery("Kndatasource.findAll").getResultList();
        contentlist = em.createNamedQuery("Knlist.findAll").getResultList();
        classcontentlist = em.createNamedQuery("Knclasscontent.findAll").getResultList();
        selectedDatasources = new ArrayList<>();
        selectedContentlist = new ArrayList<>();
        selectedclasscontentlist = new ArrayList<>();
        locale = propertymap.get("response.locale");
        contentType = propertymap.get("response.contenttype");
        characterEncoding = propertymap.get("response.characterencoding");
    }
    
    public void initTemplatelist() {
        templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
    }
    
    public void initStylesheetlist() {
        stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
    }
    
    public void initJavascriptlist() {
        javascriptlist = em.createNamedQuery("Knjavascript.findAll").getResultList();
    }
    
    public void initDatasources() {
        datasources = em.createNamedQuery("Kndatasource.findAll").getResultList();
    }
    
    public void initContentlist() {
        contentlist = em.createNamedQuery("Knlist.findAll").getResultList();
    }

    private void fillChildren(long parentid, TreeNode node) {
        List<CfSite> sitelist = em.createNamedQuery("Knsite.findByParentref").setParameter("parentref", parentid).getResultList();
        
        for (CfSite site : sitelist) {
            TreeNode tn = new DefaultTreeNode(site);
            node.getChildren().add(tn);
            fillChildren(site.getId(), tn);
        }
    }
    
    private void loadTree() {
        List<CfSite> sitelist = em.createNamedQuery("Knsite.findByParentref").setParameter("parentref", 0).getResultList();
        
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
                    CfTemplate template = (CfTemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", selectedSite.getTemplateref()).getSingleResult();
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
                    CfStylesheet stylesheet = (CfStylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", selectedSite.getStylesheetref()).getSingleResult();
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
                    Knjavascript javascript = (Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", selectedSite.getJavascriptref()).getSingleResult();
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
            Kntemplate template = (Kntemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", selectedSite.getTemplateref()).getSingleResult();
            int idx = templatelist.indexOf(template);
            selectedTemplate = templatelist.get(idx);
        } else {
            selectedTemplate = null;
        }
        if (selectedSite.getStylesheetref() != null) {
            Knstylesheet styleshet = (Knstylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", selectedSite.getStylesheetref()).getSingleResult();
            int idx = stylesheetlist.indexOf(styleshet);
            selectedStylesheet = stylesheetlist.get(idx);
        } else {
            selectedStylesheet = null;
        }
        if (selectedSite.getJavascriptref() != null) {
            Knjavascript javascript = (Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", selectedSite.getJavascriptref()).getSingleResult();
            int idx = javascriptlist.indexOf(javascript);
            selectedJavascript = javascriptlist.get(idx);
        } else {
            selectedJavascript = null;
        }
        selectedDatasources.clear();
        List<Knsitedatasource> selectedSiteDatasources = em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        for (Knsitedatasource sitedatasource : selectedSiteDatasources) {
            Kndatasource ds = (Kndatasource) em.createNamedQuery("Kndatasource.findById").setParameter("id", sitedatasource.getKnsitedatasourcePK().getDatasourceref()).getSingleResult();
            selectedDatasources.add(ds);
        }
        
        selectedContentlist.clear();
        List<Knsitelist> selectedSitecontentlist = em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        for (Knsitelist sitelist : selectedSitecontentlist) {
            Knlist cl = (Knlist) em.createNamedQuery("Knlist.findById").setParameter("id", sitelist.getKnsitelistPK().getListref()).getSingleResult();
            selectedContentlist.add(cl);
        }
        
        selectedclasscontentlist.clear();
        List<Knsitecontent> selectedClasscontentlist = em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        for (Knsitecontent sitecontent : selectedClasscontentlist) {
            Knclasscontent cc = (Knclasscontent) em.createNamedQuery("Knclasscontent.findById").setParameter("id", sitecontent.getKnsitecontentPK().getClasscontentref()).getSingleResult();
            selectedclasscontentlist.add(cc);
        }
        
        siteName = selectedSite.getName();
        aliaspath = selectedSite.getAliaspath();
        sitehtmlcompression = selectedSite.getHtmlcompression();
        characterEncoding = selectedSite.getCharacterencoding();
        contentType = selectedSite.getContenttype();
        locale = selectedSite.getLocale();
        
        saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        
        newButtonDisabled = true;
        
        FacesMessage message = new FacesMessage("Selected " + selectedSite.getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (selectedSite != null) {
            knsiteFacadeREST.remove(selectedSite);
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
            List<Knsitedatasource> sitedatasourceList = em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            for (Knsitedatasource sitedatasource : sitedatasourceList) {
                knsitedatasourceFacadeREST.remove(sitedatasource);
            }
            // Add selected siteresources
            if (selectedDatasources.size() > 0) {
                //for (int i = 0; i <= selectedDatasources.size(); i++) {
                for (Kndatasource datasource : selectedDatasources) {
                    Knsitedatasource sitedatasource = new Knsitedatasource();
                    KnsitedatasourcePK knsitedatasourcePK = new KnsitedatasourcePK();
                    knsitedatasourcePK.setSiteref(selectedSite.getId());
                    knsitedatasourcePK.setDatasourceref(datasource.getId());
                    sitedatasource.setKnsitedatasourcePK(knsitedatasourcePK);
                    knsitedatasourceFacadeREST.create(sitedatasource);
                }
            }
            
            // Delete sitelists first
            List<CfSitelist> sitelists = em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            for (CfSitelist sitelist : sitelists) {
                CfSitelistFacadeREST.remove(sitelist);
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
                    knsitelistFacadeREST.create(sitelist);
                }
            }
            
            // Delete sitecontent first
            List<CfSitecontent> contentlists = em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
            for (CfSitecontent content : contentlists) {
                knsitecontentFacadeREST.remove(content);
            }
            // Add selected sitecontent
            if (selectedclasscontentlist.size() > 0) {
                //for (int i = 0; i <= selectedDatasources.size(); i++) {
                for (Knclasscontent content : selectedclasscontentlist) {
                    CfSitecontent sitecontent = new CfSitecontent();
                    CfSitecontentPK knsitecontentPK = new CfSitecontentPK();
                    knsitecontentPK.setSiteref(selectedSite.getId());
                    knsitecontentPK.setClasscontentref(content.getId());
                    sitecontent.setCfSitecontentPK(knsitecontentPK);
                    knsitecontentFacadeREST.create(sitecontent);
                }
            }
            selectedSite.setHtmlcompression(sitehtmlcompression);
            selectedSite.setCharacterencoding(characterEncoding);
            selectedSite.setContenttype(contentType);
            selectedSite.setLocale(locale);
            selectedSite.setAliaspath(aliaspath);
            knsiteFacadeREST.edit(selectedSite);
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
            CfSite validateSite = (CfSite) em.createNamedQuery("Knsite.findByName").setParameter("name", siteName).getSingleResult();
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
            knsiteFacadeREST.create(newsite);
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
            knsitesaprfcFacadeREST.remove(selectedrfc);
            
            saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
        }
    }
    
    public void onNewRfc(ActionEvent actionEvent) {
        if ((selectedrfcgroup != null) && (selectedrfcfunction != null)) {
            Knsitesaprfc sitesaprfc = new Knsitesaprfc();
            KnsitesaprfcPK knsitesaprfcPK = new KnsitesaprfcPK();
            knsitesaprfcPK.setSiteref(selectedSite.getId());
            knsitesaprfcPK.setRfcgroup(selectedrfcgroup.getName());
            knsitesaprfcPK.setRfcfunction(selectedrfcfunction.getName());
            sitesaprfc.setKnsitesaprfcPK(knsitesaprfcPK);
            knsitesaprfcFacadeREST.create(sitesaprfc);
            
            saprfclist = em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", selectedSite.getId()).getResultList();
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
