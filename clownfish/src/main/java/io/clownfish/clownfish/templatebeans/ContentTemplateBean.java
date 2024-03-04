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
package io.clownfish.clownfish.templatebeans;

import io.clownfish.clownfish.datamodels.AssetListOutput;
import io.clownfish.clownfish.datamodels.ContentDataOutput;
import io.clownfish.clownfish.datamodels.ContentOutput;
import io.clownfish.clownfish.datamodels.DatalistOutput;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 *
 * @author sulzbachr
 */
@Scope("request")
@Component
public class ContentTemplateBean implements Serializable {
    private CfClasscontentService cfclasscontentService;
    private CfAttributcontentService cfattributcontentService;
    private CfListService cflistService;
    private CfAssetlistService cfassetlistService;
    private CfAssetlistcontentService cfassetlistcontentService;
    private CfAssetService cfassetService;
    private CfClassService cfclassService;
    private CfListcontentService cflistcontentService;
    private PropertyUtil propertyUtil;
    private ContentUtil contentUtil;
    private transient @Getter @Setter Map contentmap;
    private int useHibernate;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ContentTemplateBean.class);
    
    public ContentTemplateBean(PropertyUtil propertyUtil, ContentUtil contentutil) {
        this.contentUtil = contentutil;
        this.propertyUtil = propertyUtil;
        contentmap = new HashMap<>();
    }
    
    public void init(CfClasscontentService classcontentService, CfAttributcontentService attributcontentService, CfListService listService, CfListcontentService listcontentService, CfClassService classService, CfAssetlistService cfassetlistService, CfAssetlistcontentService cfassetlistcontentService, CfAssetService cfassetService, int useHibernate) {
        this.cfclasscontentService = classcontentService;
        this.cfattributcontentService = attributcontentService;
        this.cflistService = listService;
        this.cflistcontentService = listcontentService;
        this.cfclassService = classService;
        this.cfassetlistService = cfassetlistService;
        this.cfassetlistcontentService = cfassetlistcontentService;
        this.cfassetService = cfassetService;
        
        this.useHibernate = useHibernate;
        contentmap.clear();
    }
    
    public Map getContent(String identifier) {
        if (!identifier.isBlank()) {
            try {
                CfClasscontent classcontent = cfclasscontentService.findByName(identifier);
                contentmap.put("CO", getContentdataoutput(classcontent));
            } catch (Exception ex) {
                contentmap.put("CO", null);
            }
        } else {
            contentmap.put("CO", null);
        }
        return contentmap;
    }
    
    public Map getContent(long id) {
        if (0 != id) {
            try {
                CfClasscontent classcontent = cfclasscontentService.findById(id);
                contentmap.put("CO", getContentdataoutput(classcontent));
            } catch (Exception ex) {
                contentmap.put("CO", null);
            }
        } else {
            contentmap.put("CO", null);
        }
        return contentmap;
    }
    
    public Map getDatalist(String identifier) {
        if (!identifier.isBlank()) {
            try {
                CfList list = cflistService.findByName(identifier);
                CfClass clazz = cfclassService.findById(list.getClassref().getId());
                contentmap.put("DL", getDatalistoutput(list, clazz));
            } catch (Exception ex) {
                contentmap.put("DL", null);
            }
        } else {
            contentmap.put("DL", null);
        }
        return contentmap;
    }
    
    public Map getDatalist(long id) {
        if (0 != id) {
            try {
                CfList list = cflistService.findById(id);
                CfClass clazz = cfclassService.findById(list.getClassref().getId());
                contentmap.put("DL", getDatalistoutput(list, clazz));
            } catch (Exception ex) {
                contentmap.put("DL", null);
            }
        } else {
            contentmap.put("DL", null);
        }
        return contentmap;
    }
    
    public Map getAssetlist(String identifier) {
        if (!identifier.isBlank()) {
            try {
                CfAssetlist list = cfassetlistService.findByName(identifier);
                contentmap.put("AL", getDatalistoutput(list));
            } catch (Exception ex) {
                contentmap.put("AL", null);
            }
        } else {
            contentmap.put("AL", null);
        }
        return contentmap;
    }
    
    private ContentDataOutput getContentdataoutput(CfClasscontent classcontent) {
        List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
        ArrayList<HashMap> keyvals = contentUtil.getContentOutputKeyvalList(attributcontentList);
        HashMap keyval = contentUtil.getContentOutputKeyval(attributcontentList);
        ArrayList<String> keywords = contentUtil.getContentOutputKeywords(classcontent, true);

        ContentDataOutput contentdataoutput = new ContentDataOutput();
        contentdataoutput.setContent(classcontent);
        contentdataoutput.setKeywords(keywords);
        contentdataoutput.setKeyvals(keyvals);
        contentdataoutput.setKeyval(keyval);
        
        return contentdataoutput;
    }
    
    private DatalistOutput getDatalistoutput(CfList list, CfClass cfclass) {
        DatalistOutput datalistoutput = new DatalistOutput();
        ArrayList<ContentOutput> outputlist = new ArrayList<>();
        
        List<CfListcontent> listcontentList = cflistcontentService.findByListref(list.getId());

        List<CfClasscontent> classcontentList = new ArrayList<>();
        for (CfListcontent listcontent : listcontentList) {
            CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
            if (null != classcontent) {
                // !ToDo: #95 check AccessManager
                //if (accessmanager.checkAccess(inst_token, TYPE_CONTENT.getValue(), BigInteger.valueOf(classcontent.getId()))) {
                    classcontentList.add(classcontent);
                //}
            } else {
                LOGGER.warn("Classcontent does not exist: " + listcontent.getCfListcontentPK().getListref() + " - " + listcontent.getCfListcontentPK().getClasscontentref());
            }
        }

        if (1 == useHibernate) {
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            for (CfClasscontent classcontent : classcontentList) {

                Query query = session_tables.createQuery("FROM " + cfclass.getName() + " c WHERE cf_contentref = " + classcontent.getId());
                Map content = (Map) query.getSingleResult();
                ContentOutput co = new ContentOutput();
                co.setIdentifier(classcontent.getName());
                co.setKeyvals(contentUtil.getContentMapList(content));
                co.setKeyval(contentUtil.getContentMap(content));
                co.setKeywords(contentUtil.getContentOutputKeywords(classcontent, false));
                outputlist.add(co);
            }
            session_tables.close();
        } else {
            for (CfClasscontent classcontent : classcontentList) {    
                List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                ContentOutput co = new ContentOutput();
                co.setIdentifier(classcontent.getName());
                co.setKeyvals(contentUtil.getContentOutputKeyvalList(attributcontentList));
                co.setKeyval(contentUtil.getContentOutputKeyval(attributcontentList));
                co.setKeywords(contentUtil.getContentOutputKeywords(classcontent, false));
                outputlist.add(co);
            }
        }

        datalistoutput.setCflist(list);
        datalistoutput.setOutputlist(outputlist);
        
        return datalistoutput;
    }
    
    private AssetListOutput getDatalistoutput(CfAssetlist list) {
        AssetListOutput assetlistoutput = new AssetListOutput();
        List<CfAsset> assetcontentList = new ArrayList<>();
        List<CfAssetlistcontent> assetcontentlist = cfassetlistcontentService.findByAssetlistref(list.getId());
        for (CfAssetlistcontent alc : assetcontentlist) {
            CfAsset asset = cfassetService.findById(alc.getCfAssetlistcontentPK().getAssetref());
            assetcontentList.add(asset);
        }
        assetlistoutput.setAssetlist(list);
        assetlistoutput.setAssets(assetcontentList);
        
        return assetlistoutput;
    }
    
    /*
    public Map updateContent(RestContentParameter ucp) {
        try {
            CfClasscontent classcontent = cfclasscontentService.findByName(ucp.getContentname().trim().replaceAll("\\s+", "_"));
            try {
                if ((null != ucp.getUpdatecontentname()) && (!ucp.getUpdatecontentname().isEmpty())) {
                    CfClasscontent updateclasscontent = cfclasscontentService.findByName(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                }
            } catch (javax.persistence.NoResultException ex) {
                classcontent.setName(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                ucp.setContentname(ucp.getUpdatecontentname().trim().replaceAll("\\s+", "_"));
                cfclasscontentService.edit(classcontent);
            }

            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                CfAttribut attribut = attributcontent.getAttributref();
                // Check, if attribut exists in attributmap
                if (ucp.getAttributmap().containsKey(attribut.getName())) {
                    contentUtil.setAttributValue(attributcontent, ucp.getAttributmap().get(attribut.getName()));
                    cfattributcontentService.edit(attributcontent);
                    if (ucp.isIndexing()) {
                        contentUtil.indexContent();
                    }
                    ucp.setReturncode("OK");
                }
            }
            contentUtil.getHibernateUtil().updateContent(classcontent);
        } catch (javax.persistence.NoResultException ex) {
            ucp.setReturncode("Classcontent not found");
        }
    }
    */
}
