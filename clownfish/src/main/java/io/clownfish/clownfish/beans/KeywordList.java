/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("keywordList")
@ViewScoped
@Component
public class KeywordList {
    @Autowired CfKeywordService cfkeywordService;
    @Autowired PropertyList propertylist;
    @Autowired AssetList assetlist;
    
    private static Map<String, String> propertymap = null;
    
    private @Getter @Setter List<CfKeyword> keywordlist;
    private @Getter @Setter List<String> items;
    private @Getter @Setter CfKeyword selectedKeyword;
    private @Getter @Setter List<CfKeyword> filteredKeyword;
    
    @PostConstruct
    public void init() {
        keywordlist = cfkeywordService.findAll();
        
        if (propertymap == null) {
            // read all System Properties of the property table
            propertymap = propertylist.fillPropertyMap();
        }
    }

    public void onCreate(ActionEvent actionEvent) {
        try {
            for (String keyword : items) {
                try {
                    CfKeyword testkeyword = cfkeywordService.findByName(keyword);
                } catch (NoResultException ex) {
                    CfKeyword newkeyword = new CfKeyword();
                    newkeyword.setName(keyword);
                    cfkeywordService.create(newkeyword);
                }
            }
            keywordlist = cfkeywordService.findAll();
            assetlist.init();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
