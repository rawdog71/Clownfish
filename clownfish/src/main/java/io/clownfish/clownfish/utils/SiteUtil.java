/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author rawdog
 */
public class SiteUtil {
    @Autowired CfSitelistService cfsitelistService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClassService cfclassService;
    @Autowired CfListService cflistService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfAttributcontentService cfattributcontentService;
    
    private final ClassUtil classutil;

    public SiteUtil() {
        classutil = new ClassUtil();
    }
    
    public void getSitelist_list(CfSite cfsite, Map sitecontentmap) {
        List<CfSitelist> sitelist_list = new ArrayList<>();
        //sitelist_list.addAll(em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
        sitelist_list.addAll(cfsitelistService.findBySiteref(cfsite.getId()));
        for (CfSitelist sitelist : sitelist_list) {
            //CfList knlist = (Knlist) em.createNamedQuery("Knlist.findById").setParameter("id", sitelist.getKnsitelistPK().getListref()).getSingleResult();
            CfList cflist = cflistService.findById(sitelist.getCfSitelistPK().getListref());
            Map listcontentmap = new LinkedHashMap();

            //List<CfListcontent> contentlist = em.createNamedQuery("Knlistcontent.findByListref").setParameter("listref", knlist.getId()).getResultList();
            List<CfListcontent> contentlist = cflistcontentService.findByListref(cflist.getId());
            for (CfListcontent listcontent : contentlist) {
                //Knclasscontent classcontent = (Knclasscontent) em.createNamedQuery("Knclasscontent.findById").setParameter("id", listcontent.getKnlistcontentPK().getClasscontentref()).getSingleResult();
                CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                //CfClass cfclass = (CfClass) em.createNamedQuery("Knclass.findById").setParameter("id", classcontent.getClassref().getId()).getSingleResult();
                CfClass cfclass = cfclassService.findById(classcontent.getClassref().getId());
                List<CfAttributcontent> attributcontentlist = new ArrayList<>();
                //attributcontentlist.addAll(em.createNamedQuery("Knattributcontent.findByClasscontentref").setParameter("classcontentref", classcontent).getResultList());
                attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
                listcontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
            }
            sitecontentmap.put(cflist.getName(), listcontentmap);
        }
    }
    
    public Map getSitecontentmap(List<CfSitecontent> sitecontentlist) {
        Map sitecontentmap = new LinkedHashMap();
        for (CfSitecontent sitecontent : sitecontentlist) {
            //CfClasscontent classcontent = em.find(Knclasscontent.class, sitecontent.getKnsitecontentPK().getClasscontentref());
            CfClasscontent classcontent = cfclasscontentService.findById(sitecontent.getCfSitecontentPK().getClasscontentref());
            List<CfAttributcontent> attributcontentlist = new ArrayList<>();
            //Knclasscontent knclasscontent = em.find(Knclasscontent.class, classcontent.getId());
            //attributcontentlist.addAll(em.createNamedQuery("Knattributcontent.findByClasscontentref").setParameter("classcontentref", knclasscontent).getResultList());
            attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
            sitecontentmap.put(classcontent.getName(), classutil.getattributmap(classcontent));
            
        }
        return sitecontentmap;
    }
}
