package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.compiler.JVMLanguages;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfJavaversion;
import io.clownfish.clownfish.dbentities.CfJavaversionPK;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.lucene.SourceIndexer;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavaversionService;
import io.clownfish.clownfish.utils.CheckoutUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.JavaUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.extensions.model.monacoeditor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.primefaces.extensions.model.monaco.MonacoDiffEditorModel;

@Named("javaList")
@Scope("singleton")
@Component
public class JavaList implements ISourceContentInterface
{
    @Inject LoginBean loginbean;
    @Autowired CfJavaService cfjavaService;
    @Autowired CfJavaversionService cfjavaversionService;

    private @Getter @Setter List<CfJava> javaListe;
    private @Getter @Setter CfJava selectedJava = null;
    private @Getter @Setter String javaName = "";
    private @Getter @Setter boolean newButtonDisabled = true;
    private @Getter @Setter CfJavaversion version = null;
    private @Getter @Setter long javaversion = 0;
    private @Getter @Setter long javaversionMin = 0;
    private @Getter @Setter long javaversionMax = 0;
    private @Getter @Setter long selectedjavaversion = 0;
    private @Getter @Setter int javaLanguage = 0;
    private @Getter @Setter String selectedLanguage = "";
    private @Getter @Setter List<CfJavaversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter EditorOptions editorOptions;
    private @Getter @Setter boolean showDiff;
    private @Getter @Setter DiffEditorOptions editorOptionsDiff;
    private @Getter @Setter MonacoDiffEditorModel contentDiff;
    @Autowired private @Getter @Setter JavaUtil javaUtility;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired @Getter @Setter SourceIndexer sourceindexer;
    @Autowired @Getter @Setter CfClassCompiler classcompiler;

    final transient Logger LOGGER = LoggerFactory.getLogger(JavaList.class);

    public JavaList() {}

    @Override
    public String getContent()
    {
        if (null != selectedJava)
        {
            if (selectedjavaversion != javaversionMax)
            {
                return javaUtility.getVersion(selectedJava.getId(), selectedjavaversion);
            }
            else
            {
                javaUtility.setJavaContent(selectedJava.getContent());
                return javaUtility.getJavaContent();
            }
        }
        else
        {
            return "";
        }
    }

    @Override
    public void setContent(String content)
    {
        if (null != selectedJava)
        {
            selectedJava.setContent(content);
        }
    }

    @PostConstruct
    @Override
    public void init()
    {
        LOGGER.info("INIT JAVA START");
        try {
            sourceindexer.initJava(cfjavaService, indexService);
        } catch (IOException ex) {
            
        }
        javaName = "";
        javaListe = cfjavaService.findAll();
        javaUtility.setJavaContent("");
        checkedout = false;
        access = false;
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("java");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        editorOptionsDiff = new DiffEditorOptions();
        editorOptionsDiff.setTheme(ETheme.VS_DARK);
        editorOptionsDiff.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT JAVA END");
    }

    @Override
    public void refresh()
    {
        javaListe = cfjavaService.findAll();
    }
    
    public HashMap<Integer, String> getJvmLanguages() {
        HashMap<Integer, String> availableLanguages = new HashMap<>();
        for (JVMLanguages language : classcompiler.getcompilerlanguages().getJvm_languages().keySet()) {
            if (classcompiler.getcompilerlanguages().getJvm_languages().get(language)) {
                availableLanguages.put(language.getId(), language.getName());
            }
        }
        return availableLanguages;
    }
    
    public List<CfJava> completeText(String query) {
        String queryLowerCase = query.toLowerCase();

        return javaListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    @Override
    public void onSelect(AjaxBehaviorEvent event)
    {
        difference = false;
        showDiff = false;
        if (null != selectedJava)
        {
            javaName = selectedJava.getName();
            javaUtility.setJavaContent(selectedJava.getContent());
            javaLanguage = selectedJava.getLanguage();
            switch (selectedJava.getLanguage()) {
                case 0:
                    selectedLanguage = "java";
                    editorOptions.setLanguage("java");
                    break;
                case 1:
                    selectedLanguage = "kotlin";
                    editorOptions.setLanguage("kotlin");
                    break;
                case 2:
                    selectedLanguage = "groovy";
                    editorOptions.setLanguage("scala");
                    break;
                case 3:
                    selectedLanguage = "scala";
                    editorOptions.setLanguage("scala");
                    break;
            }
            
            versionlist = cfjavaversionService.findByJavaref(selectedJava.getId());
            difference = javaUtility.hasDifference(selectedJava);
            BigInteger co = selectedJava.getCheckedoutby();
            CheckoutUtil checkoutUtil = new CheckoutUtil();
            checkoutUtil.getCheckoutAccess(co, loginbean);
            javaversionMin = 1;
            checkedout = checkoutUtil.isCheckedout();
            access = checkoutUtil.isAccess();
            javaversionMax = versionlist.size();
            selectedjavaversion = javaversionMax;
        }
        else
        {
            javaName = "";
            checkedout = false;
            access = false;
        }
    }

    @Override
    public void onSave(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            selectedJava.setLanguage(javaLanguage);
            selectedJava.setContent(getContent());
            cfjavaService.edit(selectedJava);
            difference = javaUtility.hasDifference(selectedJava);

            FacesMessage message = new FacesMessage("Saved " + selectedJava.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    @Override
    public void onCommit(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            boolean canCommit = false;
            if (javaUtility.hasDifference(selectedJava))
            {
                canCommit = true;
            }
            if (canCommit)
            {
                try
                {
                    String content = getContent();
                    byte[] output = CompressionUtils.compress(content.getBytes(StandardCharsets.UTF_8));
                    try
                    {
                        long maxversion = cfjavaversionService.findMaxVersion(selectedJava.getId());
                        javaUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedJava.getId(), javaUtility.getCurrentVersion(), output);
                        difference = javaUtility.hasDifference(selectedJava);
                        this.javaversionMax = javaUtility.getCurrentVersion();
                        this.selectedjavaversion = this.javaversionMax;

                        FacesMessage message = new FacesMessage("Committed " + selectedJava.getName() + " version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                    catch (NullPointerException nullptr)
                    {
                        writeVersion(selectedJava.getId(), 1, output);
                        javaUtility.setCurrentVersion(1);
                        difference = javaUtility.hasDifference(selectedJava);

                        FacesMessage message = new FacesMessage("Committed " + selectedJava.getName() + " version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                    sourceindexer.indexJava(selectedJava);
                }
                catch (IOException ex)
                {
                    LOGGER.error(ex.getMessage());
                }
            }
            else
            {
                difference = javaUtility.hasDifference(selectedJava);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedJava.getName() + " version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    @Override
    public void onCheckIn(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            selectedJava.setCheckedoutby(BigInteger.valueOf(0));
            selectedJava.setContent(getContent());
            cfjavaService.edit(selectedJava);

            difference = javaUtility.hasDifference(selectedJava);
            checkedout = false;

            FacesMessage message = new FacesMessage("Checked in " + selectedJava.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    @Override
    public void onCheckOut(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            boolean canCheckout = false;
            CfJava checkjava = cfjavaService.findById(selectedJava.getId());
            BigInteger co = checkjava.getCheckedoutby();
            if (null != co)
            {
                if (co.longValue() == 0)
                {
                    canCheckout = true;
                }
            }
            else
            {
                canCheckout = true;
            }

            if (canCheckout)
            {
                selectedJava.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedJava.setContent(getContent());
                cfjavaService.edit(selectedJava);
                difference = javaUtility.hasDifference(selectedJava);
                checkedout = true;
                showDiff = false;

                FacesMessage message = new FacesMessage("Checked out " + selectedJava.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            else
            {
                access = false;
                FacesMessage message = new FacesMessage("Could not check out " + selectedJava.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    @Override
    public void onChangeName(ValueChangeEvent changeEvent)
    {
        if (!javaName.isBlank())
        {
            try
            {
                cfjavaService.findByName(javaName);
                newButtonDisabled = true;
            }
            catch (NoResultException ex)
            {
                newButtonDisabled = javaName.isEmpty();
            }
        }
        else
        {
            newButtonDisabled = true;
        }
    }

    @Override
    public void onCreate(ActionEvent actionEvent)
    {
        try
        {
            if (!javaName.isBlank())
            {
                CfJava newjava = new CfJava();
                newjava.setName(javaName);
                
                switch (javaLanguage) {
                    case 0:
                        newjava.setContent("package io.clownfish.java;\n\n"
                            + "public class " + javaName + "\n{\n\n}");
                        break;
                    case 1:
                        newjava.setContent("package io.clownfish.kotlin;\n\n"
                            + "public class " + javaName + "\n{\n\n}");
                        break;
                    case 2:
                        newjava.setContent("package io.clownfish.groovy;\n\n"
                            + "public class " + javaName + "\n{\n\n}");
                        break;
                    case 3:
                        newjava.setContent("package io.clownfish.scala\n\n"
                            + "class " + javaName + "\n{\n\n}");
                        break;
                }
                newjava.setLanguage(javaLanguage);
                cfjavaService.create(newjava);
                javaListe = cfjavaService.findAll();
                javaName = "";
            }
            else
            {
                FacesMessage message = new FacesMessage("Please enter Java name");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
        catch (ConstraintViolationException ex)
        {
            LOGGER.error(ex.getMessage());
        }
    }

    @Override
    public void onDelete(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            cfjavaService.delete(selectedJava);
            javaListe = cfjavaService.findAll();

            FacesMessage message = new FacesMessage("Deleted " + selectedJava.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    @Override
    public void writeVersion(long javaref, long version, byte[] content)
    {
        CfJavaversionPK javaversionpk = new CfJavaversionPK();
        javaversionpk.setJavaref(javaref);
        javaversionpk.setVersion(version);

        CfJavaversion cfjavaversion = new CfJavaversion();
        cfjavaversion.setCfJavaversionPK(javaversionpk);
        cfjavaversion.setContent(content);
        cfjavaversion.setTstamp(new Date());
        cfjavaversion.setCommitedby(BigInteger.valueOf(loginbean.getCfuser().getId()));
        cfjavaversionService.create(cfjavaversion);
    }

    @Override
    public void onVersionSelect(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            javaUtility.getVersion(version.getCfJavaversionPK().getJavaref(), version.getCfJavaversionPK().getVersion());
        }
    }

    @Override
    public void onSlideEnd(SlideEndEvent event)
    {
        selectedjavaversion = (int) event.getValue();
        if (selectedjavaversion <= javaversionMin)
        {
            selectedjavaversion = javaversionMin;
        }
        if (selectedjavaversion >= javaversionMax)
        {
            selectedjavaversion = javaversionMax;
        }
        showDiff = (selectedjavaversion < javaversionMax);
        if (showDiff) {
            contentDiff = new MonacoDiffEditorModel(javaUtility.getVersion(selectedJava.getId(), selectedjavaversion), javaUtility.getVersion(selectedJava.getId(), javaversionMax));
        }
    }

    @Override
    public void onVersionChanged() {
        if (javaversion <= javaversionMin)
            javaversion = javaversionMin;

        if (javaversion >= javaversionMax)
            javaversion = javaversionMax;

        selectedjavaversion = javaversion;
    }

    @Override
    public void onChange(ActionEvent actionEvent)
    {
        if (null != selectedJava)
        {
            selectedJava.setLanguage(javaLanguage);
            selectedJava.setName(javaName);
            cfjavaService.edit(selectedJava);
            difference = javaUtility.hasDifference(selectedJava);
            refresh();

            FacesMessage message = new FacesMessage("Changed " + selectedJava.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        else
        {
            FacesMessage message = new FacesMessage("No Java selected. Nothing changed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}