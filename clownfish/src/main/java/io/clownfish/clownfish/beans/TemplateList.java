package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.dbentities.CfTemplateversionPK;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sulzbachr
 */
@ManagedBean(name="templatelist")
@Transactional
@ViewScoped
public class TemplateList {
    @Inject
    LoginBean loginbean;
    
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    
    private @Getter @Setter List<CfTemplate> templatelist;
    private @Getter @Setter CfTemplate selectedTemplate = null;
    private @Getter @Setter String templateName;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter int templateScriptLanguage = 0;
    private String selectedScriptlanguage = "";
    private @Getter @Setter CfTemplateversion version = null;
    private @Getter @Setter List<CfTemplateversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter TemplateUtil templateUtility;
    

    public TemplateList() {
    }
    
    public String getTemplateContent() {
        if (selectedTemplate != null) {
            templateUtility.setTemplateContent(selectedTemplate.getContent());
            return templateUtility.getTemplateContent();
        } else {
            return "";
        }
    }
    
    public void setTemplateContent(String content) {
        if (selectedTemplate != null) {
            selectedTemplate.setContent(content);
        }
    }

    @PostConstruct
    public void init() {
        templateUtility = new TemplateUtil();
        templateName = "";
        //templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
        templatelist = cftemplateService.findAll();
        templateUtility.setTemplateContent("");
        checkedout = false;
        access = false;
    }
    
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        templateName = selectedTemplate.getName();
        templateUtility.setTemplateContent(selectedTemplate.getContent());
        templateScriptLanguage = selectedTemplate.getScriptlanguage();
        if (selectedTemplate.getScriptlanguage() == 0) {
            selectedScriptlanguage = "freemarker";
        } else {
            selectedScriptlanguage = "velocity";
        }
        //versionlist = em.createNamedQuery("Kntemplateversion.findByTemplateref").setParameter("templateref", selectedTemplate.getId()).getResultList();
        versionlist = cftemplateversionService.findByTemplateref(selectedTemplate.getId());
        difference = templateUtility.hasDifference(selectedTemplate);
        BigInteger co = selectedTemplate.getCheckedoutby();
        if (co != null) {
            if (co.longValue() > 0) {
                if (co.longValue() == loginbean.getCfuser().getId()) {
                    checkedout = true;
                    access = true;
                } else {
                    checkedout = false;
                    access = false;
                }
            } else {
                checkedout = false;
                access = true;
            }
        } else {
            checkedout = false;
            access = true;
        }
    }
    
    public void onSave(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            selectedTemplate.setContent(getTemplateContent());
            //kntemplateFacadeREST.edit(selectedTemplate);
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            
            FacesMessage message = new FacesMessage("Saved " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            boolean canCommit = false;
            if (templateUtility.hasDifference(selectedTemplate)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getTemplateContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        //long maxversion = (long) em.createNamedQuery("Kntemplateversion.findMaxVersion").setParameter("templateref", selectedTemplate.getId()).getSingleResult();
                        long maxversion = cftemplateversionService.findMaxVersion(selectedTemplate.getId());
                        templateUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedTemplate.getId(), templateUtility.getCurrentVersion(), output);
                        difference = templateUtility.hasDifference(selectedTemplate);

                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedTemplate.getId(), 1, output);
                        templateUtility.setCurrentVersion(1);
                        difference = templateUtility.hasDifference(selectedTemplate);

                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TemplateList.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                difference = templateUtility.hasDifference(selectedTemplate);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedTemplate.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            boolean canCheckout = false;
            //Kntemplate checktemplate = (Kntemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", selectedTemplate.getId()).getSingleResult();
            CfTemplate checktemplate = cftemplateService.findById(selectedTemplate.getId());
            BigInteger co = checktemplate.getCheckedoutby();
            if (co != null) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedTemplate.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedTemplate.setContent(getTemplateContent());
                //kntemplateFacadeREST.edit(selectedTemplate);
                cftemplateService.edit(selectedTemplate);
                difference = templateUtility.hasDifference(selectedTemplate);
                checkedout = true;

                FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedTemplate.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            selectedTemplate.setCheckedoutby(BigInteger.valueOf(0));
            selectedTemplate.setContent(getTemplateContent());
            //kntemplateFacadeREST.edit(selectedTemplate);
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            //Kntemplate validateTemplate = (Kntemplate) em.createNamedQuery("Kntemplate.findByName").setParameter("name", templateName).getSingleResult();
            CfTemplate validateTemplate = cftemplateService.findByName(templateName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = templateName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfTemplate newtemplate = new CfTemplate();
            newtemplate.setName(templateName);
            newtemplate.setContent("//"+templateName);
            newtemplate.setScriptlanguage(templateScriptLanguage);
            //kntemplateFacadeREST.create(newtemplate);
            cftemplateService.create(newtemplate);
            //templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
            templatelist = cftemplateService.findAll();
            templateName = "";
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            //kntemplateFacadeREST.remove(selectedTemplate);
            cftemplateService.delete(selectedTemplate);
            //templatelist = em.createNamedQuery("Kntemplate.findAll").getResultList();
            templatelist = cftemplateService.findAll();
            
            FacesMessage message = new FacesMessage("Deleted " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public String getSelectedScriptlanguage() {
        return selectedScriptlanguage;
    }

    public void setSelectedScriptlanguage(String selectedScriptlanguage) {
        this.selectedScriptlanguage = selectedScriptlanguage;
    }
    
    private void writeVersion(long templateref, long version, byte[] content) {
        CfTemplateversionPK templateversionpk = new CfTemplateversionPK();
        templateversionpk.setTemplateref(templateref);
        templateversionpk.setVersion(version);

        CfTemplateversion cftemplateversion = new CfTemplateversion();
        cftemplateversion.setCfTemplateversionPK(templateversionpk);
        cftemplateversion.setContent(content);
        cftemplateversion.setTstamp(new Date());
        cftemplateversion.setCommitedby(BigInteger.valueOf(loginbean.getCfuser().getId()));
        
        //kntemplateversionFacadeREST.create(kntemplateversion);
        cftemplateversionService.create(cftemplateversion);
    }
    
    public void onVersionSelect(ActionEvent actionEvent) {
        if (selectedTemplate != null) {
            String versioncontent = templateUtility.getVersion(version.getCfTemplateversionPK().getTemplateref(), version.getCfTemplateversionPK().getVersion());
        }
    }
}
