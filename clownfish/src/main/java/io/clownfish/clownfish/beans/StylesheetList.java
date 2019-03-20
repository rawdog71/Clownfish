package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
import io.clownfish.clownfish.dbentities.CfStylesheetversionPK;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.StylesheetUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("stylesheetList")
@Transactional
@ViewScoped
@Component
public class StylesheetList {
    @Inject
    LoginBean loginbean;
    
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    
    private @Getter @Setter List<CfStylesheet> stylesheetListe;
    private @Getter @Setter CfStylesheet selectedStylesheet = null;
    private @Getter @Setter String stylesheetName;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter CfStylesheetversion version = null;
    private @Getter @Setter List<CfStylesheetversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter StylesheetUtil stylesheetUtility;

    public StylesheetList() {
    }
   
    public String getStylesheetContent() {
        if (selectedStylesheet != null) {
            stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
            return stylesheetUtility.getStyelsheetContent();
        } else {
            return "";
        }
    }
    
    public void setStylesheetContent(String content) {
        if (selectedStylesheet != null) {
            selectedStylesheet.setContent(content);
        }
    }

    @PostConstruct
    public void init() {
        stylesheetUtility = new StylesheetUtil();
        stylesheetName = "";
        //stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
        stylesheetListe = cfstylesheetService.findAll();
        stylesheetUtility.setStyelsheetContent("");
        checkedout = false;
        access = false;
    }
    
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        stylesheetName = selectedStylesheet.getName();
        stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
        //versionlist = em.createNamedQuery("Knstylesheetversion.findByStylesheetref").setParameter("stylesheetref", selectedStylesheet.getId()).getResultList();
        versionlist = cfstylesheetversionService.findByStylesheetref(selectedStylesheet.getId());
        difference = stylesheetUtility.hasDifference(selectedStylesheet);
        BigInteger co = selectedStylesheet.getCheckedoutby();
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
        if (selectedStylesheet != null) {
            selectedStylesheet.setContent(getStylesheetContent());
            //knstylesheetFacadeREST.edit(selectedStylesheet);
            cfstylesheetService.edit(selectedStylesheet);
            
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            
            FacesMessage message = new FacesMessage("Saved " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (selectedStylesheet != null) {
            boolean canCommit = false;
            if (stylesheetUtility.hasDifference(selectedStylesheet)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getStylesheetContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        //long maxversion = (long) em.createNamedQuery("Knstylesheetversion.findMaxVersion").setParameter("stylesheetref", selectedStylesheet.getId()).getSingleResult();
                        long maxversion = cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId());
                        stylesheetUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedStylesheet.getId(), stylesheetUtility.getCurrentVersion(), output);
                        difference = stylesheetUtility.hasDifference(selectedStylesheet);

                        FacesMessage message = new FacesMessage("Commited " + selectedStylesheet.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedStylesheet.getId(), 1, output);
                        stylesheetUtility.setCurrentVersion(1);
                        difference = stylesheetUtility.hasDifference(selectedStylesheet);

                        FacesMessage message = new FacesMessage("Commited " + selectedStylesheet.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TemplateList.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                difference = stylesheetUtility.hasDifference(selectedStylesheet);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedStylesheet.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (selectedStylesheet != null) {
            selectedStylesheet.setCheckedoutby(BigInteger.valueOf(0));
            selectedStylesheet.setContent(getStylesheetContent());
            //knstylesheetFacadeREST.edit(selectedStylesheet);
            cfstylesheetService.edit(selectedStylesheet);
            
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked In " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (selectedStylesheet != null) {
            boolean canCheckout = false;
            //Knstylesheet checktemplate = (Knstylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", selectedStylesheet.getId()).getSingleResult();
            CfStylesheet checkstylesheet = cfstylesheetService.findById(selectedStylesheet.getId());
            BigInteger co = checkstylesheet.getCheckedoutby();
            if (co != null) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedStylesheet.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedStylesheet.setContent(getStylesheetContent());
                //knstylesheetFacadeREST.edit(selectedStylesheet);
                cfstylesheetService.edit(checkstylesheet);
                
                difference = stylesheetUtility.hasDifference(selectedStylesheet);
                checkedout = true;

                FacesMessage message = new FacesMessage("Checked Out " + selectedStylesheet.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedStylesheet.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            //Knstylesheet validateStylesheet = (Knstylesheet) em.createNamedQuery("Knstylesheet.findByName").setParameter("name", stylesheetName).getSingleResult();
            CfStylesheet validateStylesheet = cfstylesheetService.findByName(stylesheetName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = stylesheetName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfStylesheet newstylesheet = new CfStylesheet();
            newstylesheet.setName(stylesheetName);
            newstylesheet.setContent("//"+stylesheetName);
            //knstylesheetFacadeREST.create(newstylesheet);
            cfstylesheetService.create(newstylesheet);
            //stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
            stylesheetListe = cfstylesheetService.findAll();
            stylesheetName = "";
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (selectedStylesheet != null) {
            //knstylesheetFacadeREST.remove(selectedStylesheet);
            cfstylesheetService.delete(selectedStylesheet);
            // stylesheetlist = em.createNamedQuery("Knstylesheet.findAll").getResultList();
            stylesheetListe = cfstylesheetService.findAll();
            FacesMessage message = new FacesMessage("Deleted " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    private void writeVersion(long stylesheetref, long version, byte[] content) {
        CfStylesheetversionPK stylesheetversionpk = new CfStylesheetversionPK();
        stylesheetversionpk.setStylesheetref(stylesheetref);
        stylesheetversionpk.setVersion(version);

        CfStylesheetversion cfstylesheetversion = new CfStylesheetversion();
        cfstylesheetversion.setCfStylesheetversionPK(stylesheetversionpk);
        cfstylesheetversion.setContent(content);
        cfstylesheetversion.setTstamp(new Date());
        //knstylesheetversionFacadeREST.create(knstylesheetversion);
        cfstylesheetversionService.create(cfstylesheetversion);
    }
    
    public void onVersionSelect(ActionEvent actionEvent) {
        if (selectedStylesheet != null) {
            String versioncontent = stylesheetUtility.getVersion(version.getCfStylesheetversionPK().getStylesheetref(), version.getCfStylesheetversionPK().getVersion());
        }
    }
}
