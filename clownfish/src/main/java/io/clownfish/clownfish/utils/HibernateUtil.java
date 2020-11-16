/*
 * Copyright 2020 sulzbachr.
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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Component
public class HibernateUtil {

    private static CfClassService cfclassservice;
    private static CfAttributService cfattributservice;
    private static CfClasscontentService cfclasscontentService;
    private static CfAttributcontentService cfattributcontentService;
    private static CfListcontentService cflistcontentService;
    private static CfKeywordService cfkeywordService;
    private static CfClasscontentKeywordService cfclasscontentkeywordService;
    private static @Getter @Setter HashMap<String, Session> classsessions = new HashMap<>();
    private static ServiceStatus serviceStatus;
    //private static @Getter @Setter Session session_tables;
    //private static @Getter @Setter Session session_relations;

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);
    
    public void init(ServiceStatus serviceStatus, CfClassService cfclassservice, CfAttributService cfattributservice, CfClasscontentService cfclasscontentService, CfAttributcontentService cfattributcontentService, CfListcontentService cflistcontentService, CfClasscontentKeywordService cfclasscontentkeywordService, CfKeywordService cfkeywordService) {
        this.cfclassservice = cfclassservice;
        this.cfattributservice = cfattributservice;
        this.cfclasscontentService = cfclasscontentService;
        this.cfattributcontentService = cfattributcontentService;
        this.cflistcontentService = cflistcontentService;
        this.serviceStatus = serviceStatus;
        this.cfkeywordService = cfkeywordService;
        this.cfclasscontentkeywordService = cfclasscontentkeywordService;
    }
    
    public static synchronized void generateTablesDatamodel(int initHibernate) {
        serviceStatus.setMessage("Generating dynamic tables");
        serviceStatus.setOnline(false);
        Document xmldoc = DocumentHelper.createDocument();
        xmldoc.setName("tables");
        Element root = xmldoc.addElement("hibernate-mapping");
        List<CfClass> classlist = cfclassservice.findAll();
        for (CfClass clazz : classlist) {
            Element elementclass = root.addElement("class");
            elementclass.addAttribute("entity-name", clazz.getName());
            elementclass.addAttribute("table", "usr_" + clazz.getName());

            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            int idCount = hasIdentifier(attributlist);
            // Set the primary key
            Element elementid = elementclass.addElement("id");
            elementid.addAttribute("name", "cf_id");
            elementid.addAttribute("column", "CF_ID__");
            elementid.addAttribute("type", "long");
            Element elementgenerator = elementid.addElement("generator");
            elementgenerator.addAttribute("class", "native");
            makePrimaryKey(attributlist, elementclass, idCount);
            // Set the properties
            for (CfAttribut attribut : attributlist) {
                if (!attribut.getIdentity()) {
                    Element elementproperty = elementclass.addElement("property");
                    elementproperty.addAttribute("name", attribut.getName());
                    elementproperty.addAttribute("column", attribut.getName() + "_");
                    elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName()));
                    elementproperty.addAttribute("not-null", "false");
                    if (attribut.getIsindex()) {
                        elementproperty.addAttribute("index", "idx_" + attribut.getName());
                    }
                }                
            }
            Element elementproperty = elementclass.addElement("property");
            elementproperty.addAttribute("name", "cf_contentref");
            elementproperty.addAttribute("column", "CF__CONTENTREF__");
            elementproperty.addAttribute("type", "long");
            elementproperty.addAttribute("not-null", "true");
            elementproperty.addAttribute("index", "idx_cf_contentref");
        }
        System.out.println(xmldoc.asXML());
        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes())).buildMetadata().buildSessionFactory();
        Session session_tables = sessionFactory.openSession();
        classsessions.put("tables", session_tables);
        for (CfClass clazz : classlist) {
            if (initHibernate > 0) {
                fillTable(clazz.getName(), session_tables);
            }
        }
        session_tables.close();

        LOGGER.info("Data Model created");
        serviceStatus.setMessage("online");
        serviceStatus.setOnline(true);
    }
    
    public static synchronized void generateTablesDatamodel(String classname, int initHibernate) {
        serviceStatus.setMessage("Generating dynamic tables");
        serviceStatus.setOnline(false);
        Document xmldoc = DocumentHelper.createDocument();
        xmldoc.setName("tables");
        Element root = xmldoc.addElement("hibernate-mapping");
        CfClass cfclass = cfclassservice.findByName(classname);
        
        Element elementclass = root.addElement("class");
        elementclass.addAttribute("entity-name", cfclass.getName());
        elementclass.addAttribute("table", "usr_" + cfclass.getName());

        List<CfAttribut> attributlist = cfattributservice.findByClassref(cfclass);
        int idCount = hasIdentifier(attributlist);
        // Set the primary key
        Element elementid = elementclass.addElement("id");
        elementid.addAttribute("name", "cf_id");
        elementid.addAttribute("column", "CF_ID__");
        elementid.addAttribute("type", "long");
        Element elementgenerator = elementid.addElement("generator");
        elementgenerator.addAttribute("class", "native");
        makePrimaryKey(attributlist, elementclass, idCount);
        // Set the properties
        for (CfAttribut attribut : attributlist) {
            if (!attribut.getIdentity()) {
                Element elementproperty = elementclass.addElement("property");
                elementproperty.addAttribute("name", attribut.getName());
                elementproperty.addAttribute("column", attribut.getName() + "_");
                elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName()));
                elementproperty.addAttribute("not-null", "false");
                if (attribut.getIsindex()) {
                    elementproperty.addAttribute("index", "idx_" + attribut.getName());
                }
            }                
        }
        Element elementproperty = elementclass.addElement("property");
        elementproperty.addAttribute("name", "cf_contentref");
        elementproperty.addAttribute("column", "CF__CONTENTREF__");
        elementproperty.addAttribute("type", "long");
        elementproperty.addAttribute("not-null", "true");
        elementproperty.addAttribute("index", "idx_cf_contentref");
        
        System.out.println(xmldoc.asXML());
        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes())).buildMetadata().buildSessionFactory();
        Session session_tables = sessionFactory.openSession();
        classsessions.put("tables", session_tables);
        if (initHibernate > 0) {
            fillTable(classname, session_tables);
        }
        session_tables.close();

        LOGGER.info("Data Model created");
        serviceStatus.setMessage("online");
        serviceStatus.setOnline(true);
    }

    private static void fillTable(String classname, Session session) {
        CfClass cfclass = cfclassservice.findByName(classname);
        List<CfClasscontent> classcontentlist = cfclasscontentService.findByClassref(cfclass);

        for (CfClasscontent classcontent : classcontentlist) {
            LOGGER.info("FILLTABLE:" + classname);
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
            Map entity = fillEntity(new HashMap(), classcontent, attributcontentlist);
            try {
                Transaction tx = session.beginTransaction();
                session.save(classname, entity);
                tx.commit();
            } catch (org.hibernate.id.IdentifierGenerationException ex) {
                LOGGER.warn("NOT SAVED:" + classcontent.getName());
            }
        }
    }
    
    public void insertContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
        
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        Map entity = fillEntity(new HashMap(), classcontent, attributcontentlist);
        try {
            Transaction tx = session_tables.beginTransaction();
            session_tables.save(classname, entity);
            tx.commit();
            //session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        } finally {
            session_tables.close();
        }
    }
    
    public void updateContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
        Map entity = (Map) session_tables.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        entity = fillEntity(entity, classcontent, attributcontentlist);
        try {
            Transaction tx = session_tables.beginTransaction();
            session_tables.update(classname, entity);
            tx.commit();
            //session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        } finally {
            session_tables.close();
        }
    }
    
    public void deleteContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
        
        
        Map entity = (Map) session_tables.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        entity = fillEntity(entity, classcontent, attributcontentlist);
        try {
            Transaction tx = session_tables.beginTransaction();
            session_tables.delete(classname, entity);
            tx.commit();
            //session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        } finally {
            session_tables.close();
        }
    }

    private static String getHibernateType(String clownfishtype) {
        switch (clownfishtype) {
            case "boolean":
                return "boolean";
            case "string":
                return "string";
            case "hashstring":
                return "string";
            case "integer":
                return "long";
            case "real":
                return "double";
            case "htmltext":
                return "text";
            case "markdown":
                return "text";
            case "datetime":
                return "timestamp";
            case "media":
                return "long";
            case "text":
                return "text";
            case "classref":
                return "string";
            case "assetref":
                return "string";
            default:
                return null;
        }
    }
    
    private static int hasIdentifier(List<CfAttribut> attributlist) {
        int count = 0;
        for (CfAttribut attribut : attributlist) {
            if (attribut.getIdentity()) {
                count += 1;
            }
        }
        return count;
    }
    
    private static void makePrimaryKey(List<CfAttribut> attributlist, Element elementclass, int idcount) {
        if (idcount == 1) {
            for (CfAttribut attribut : attributlist) {
                if (attribut.getIdentity()) {
                    Element elementproperty = elementclass.addElement("property");
                    elementproperty.addAttribute("name", attribut.getName());
                    elementproperty.addAttribute("column", attribut.getName() + "_");
                    elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName()));
                    elementproperty.addAttribute("index", "idx_" + attribut.getName());
                }
            }
        } else {
            for (CfAttribut attribut : attributlist) {
                if (attribut.getIdentity()) {
                    Element elementkeyproperty = elementclass.addElement("property");
                    elementkeyproperty.addAttribute("name", attribut.getName());
                    elementkeyproperty.addAttribute("column", attribut.getName() + "_");
                    elementkeyproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName()));
                    elementkeyproperty.addAttribute("index", "idx_" + attribut.getName());
                }
            }
        }
    }
    
    public static synchronized void generateRelationsDatamodel(int initHibernate) {
        Document xmldoc = DocumentHelper.createDocument();
        xmldoc.setName("relations");
        Element root = xmldoc.addElement("hibernate-mapping");
        List<CfClass> classlist = cfclassservice.findAll();
        for (CfClass clazz : classlist) {
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                switch (attribut.getAttributetype().getName()) {
                    case "classref":
                        Element elementclass = root.addElement("class");
                        elementclass.addAttribute("entity-name", clazz.getName() + "_" + attribut.getName());
                        elementclass.addAttribute("table", "usr_rel_" + clazz.getName() + "_" + attribut.getName());
                        
                        Element elementproperty = elementclass.addElement("composite-id");
                        elementproperty.addAttribute("name", "id_");
                        
                        Element elementkeyproperty1 = elementproperty.addElement("key-property");
                        elementkeyproperty1.addAttribute("name", clazz.getName() + "_ref");
                        elementkeyproperty1.addAttribute("column", clazz.getName() + "_ref_");
                        elementkeyproperty1.addAttribute("type", "long");
                        
                        Element elementkeyproperty2 = elementproperty.addElement("key-property");
                        elementkeyproperty2.addAttribute("name", attribut.getName() + "_ref");
                        elementkeyproperty2.addAttribute("column", attribut.getName() + "_ref_");
                        elementkeyproperty2.addAttribute("type", "long");
                        
                        Element elementproperty3 = elementclass.addElement("property");
                        elementproperty3.addAttribute("name", clazz.getName() + "_usr_ref");
                        elementproperty3.addAttribute("column", clazz.getName() + "_usr_ref_");
                        elementproperty3.addAttribute("type", "long");
                        elementproperty3.addAttribute("not-null", "false");
                        
                        Element elementproperty4 = elementclass.addElement("property");
                        elementproperty4.addAttribute("name", attribut.getName() + "_usr_ref");
                        elementproperty4.addAttribute("column", attribut.getName() + "_usr_ref_");
                        elementproperty4.addAttribute("type", "long");
                        elementproperty4.addAttribute("not-null", "false");
                        break;
                    case "assetref":
                        break;            
                }
            }
        }
        System.out.println(xmldoc.asXML());
        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes())).buildMetadata().buildSessionFactory();
        Session session_relations = sessionFactory.openSession();
        Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
        classsessions.put("relations", session_relations);       
        for (CfClass clazz : classlist) {
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                switch (attribut.getAttributetype().getName()) {
                    case "classref":        
                        if (initHibernate > 0) {
                            fillRelation(clazz.getName(), attribut.getName(), session_tables, session_relations);
                        }
                        break;
                }
            }
        }
        session_tables.close();
        session_relations.close();
        LOGGER.info("Data Relations created");
    }
    
    private static void fillRelation(String classname, String attributname, Session session_tables, Session session_relations) {
        CfClass cfclass = cfclassservice.findByName(classname);
        List<CfClasscontent> classcontentlist = cfclasscontentService.findByClassref(cfclass);

        for (CfClasscontent classcontent : classcontentlist) {
            LOGGER.info("FILLRELATION:" + classname);
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                if (0 == attributcontent.getAttributref().getName().compareToIgnoreCase(attributname)) {
                    CfList contentclassref = attributcontent.getClasscontentlistref();
                    if (null != contentclassref) {
                        //System.out.println(contentclassref.getName());
                        List<CfListcontent> listcontentlist = cflistcontentService.findByListref(contentclassref.getId());
                        for (CfListcontent listcontent : listcontentlist) {
                            Map content = (Map) session_tables.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
                            Map referenz = (Map) session_tables.createQuery("FROM " + attributcontent.getAttributref().getRelationref().getName() + " c WHERE c.cf_contentref = " + listcontent.getCfListcontentPK().getClasscontentref()).getSingleResult();
                            Map entity = new HashMap();
                            Map id = new HashMap();
                            id.put(classname + "_ref", classcontent.getId());
                            id.put(attributname + "_ref", listcontent.getCfListcontentPK().getClasscontentref());
                            entity.put("id_", id);
                            entity.put(classname + "_usr_ref", content.get("cf_id"));
                            entity.put(attributname + "_usr_ref", referenz.get("cf_id"));
                            try {
                                Transaction tx = session_relations.beginTransaction();
                                session_relations.save(classname + "_" + attributname, entity);
                                tx.commit();
                            } catch (Exception ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void updateRelation(CfList list) {
        String referenzname = "";
        if (null != list) {
            referenzname = list.getClassref().getName();
            //Session session_referenz = classsessions.get("relations").getSessionFactory().openSession();        
            if (null != list) {
                Session session_relations = classsessions.get("relations").getSessionFactory().openSession();
                Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
                try {
                    List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByContentclassRef(list);
                    for (CfAttributcontent attributcontent : attributcontentlist) {
                        String classname = attributcontent.getClasscontentref().getClassref().getName();
                        String attributname = attributcontent.getAttributref().getName();
                        String refname = attributcontent.getClasscontentref().getClassref().getName() + "_" + attributname;

                        Query q = session_relations.createQuery("DELETE FROM " + refname + " WHERE " + classname + "_ref_ = " + attributcontent.getClasscontentref().getId());

                        Transaction tx = session_relations.beginTransaction();
                        //tx = session_relations.getTransaction();
                        int count = q.executeUpdate();

                        List<CfListcontent> listcontentlist = cflistcontentService.findByListref(list.getId());
                        for (CfListcontent listcontent : listcontentlist) {
                            Map content = (Map) session_tables.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + attributcontent.getClasscontentref().getId()).getSingleResult();
                            Map referenz = (Map) session_tables.createQuery("FROM " + attributcontent.getAttributref().getRelationref().getName() + " c WHERE c.cf_contentref = " + listcontent.getCfListcontentPK().getClasscontentref()).getSingleResult();
                            Map entity = new HashMap();
                            Map id = new HashMap();
                            id.put(classname + "_ref", attributcontent.getClasscontentref().getId());
                            id.put(attributname + "_ref", listcontent.getCfListcontentPK().getClasscontentref());
                            entity.put("id_", id);
                            entity.put(classname + "_usr_ref", content.get("cf_id"));
                            entity.put(attributname + "_usr_ref", referenz.get("cf_id"));
                            session_relations.save(classname + "_" + attributname, entity);
                        }                        
                        tx.commit();
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                } finally {   
                    session_tables.close();
                    session_relations.close();
                }
            }
            //session_referenz.close();
        }
    }
    
    public void deleteRelation(CfList list, CfClasscontent classcontent) { 
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByContentclassRef(list);
        for (CfAttributcontent attributcontent : attributcontentlist) {
            if (attributcontent.getClasscontentref().getId() == classcontent.getId()) {
                String classname = attributcontent.getClasscontentref().getClassref().getName();
                String attributname = attributcontent.getAttributref().getName();
                String refname = attributcontent.getClasscontentref().getClassref().getName() + "_" + attributname;
                Session session_relations = classsessions.get("relations").getSessionFactory().openSession();
                Query q = session_relations.createQuery("DELETE FROM " + refname + " WHERE " + classname + "_ref_ = " + attributcontent.getClasscontentref().getId());
                try {
                    Transaction tx = session_relations.beginTransaction();
                    int count = q.executeUpdate();                     
                    tx.commit();
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                } finally {
                    session_relations.close();
                }
            }
        }
    }
    
    private static Map fillEntity(Map entity, CfClasscontent classcontent, List<CfAttributcontent> attributcontentlist) {
        entity.put("cf_contentref", classcontent.getId());
        for (CfAttributcontent attributcontent : attributcontentlist) {
            switch (attributcontent.getAttributref().getAttributetype().getName()) {
                case "boolean":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentBoolean());
                    break;
                case "string":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    break;
                case "integer":
                    if (null != attributcontent.getContentInteger()) {
                        entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().longValue());
                    } else {
                        entity.put(attributcontent.getAttributref().getName(), null);
                    }
                    break;
                case "real":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentReal());
                    break;
                case "htmltext":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    break;
                case "datetime":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentDate());
                    break;
                case "hashstring":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    break;
                case "media":
                    if (null != attributcontent.getContentInteger()) {
                        entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().longValue());
                    } else {
                        entity.put(attributcontent.getAttributref().getName(), null);
                    }
                    break;
                case "text":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    break;
                case "markdown":
                    entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    break;
                case "classref":
                    if (null != attributcontent.getClasscontentlistref()) {
                        entity.put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentlistref().getName());
                    } else {
                        entity.put(attributcontent.getAttributref().getName(), null);
                    }
                    break;
                case "assetref":
                    if (null != attributcontent.getAssetcontentlistref()) {
                        entity.put(attributcontent.getAttributref().getName(), attributcontent.getAssetcontentlistref().getName());
                    } else {
                        entity.put(attributcontent.getAttributref().getName(), null);
                    }
                    break;
            }
        }
        return entity;
    }
    
    public Map getContent(String tablename, long contentid) {
        Session session_tables = getClasssessions().get("tables").getSessionFactory().openSession();
        Query query = null;
        query = session_tables.createQuery("FROM " + tablename + " c WHERE cf_contentref = " + contentid);
        Map contentmap = (Map) query.getSingleResult();
        session_tables.close();
        /* add keywords  */
        List<CfClasscontentkeyword> contentkeywordlist;
        contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(contentid);
        if (contentkeywordlist.size() > 0) {
            ArrayList listcontentmap = new ArrayList();
            contentkeywordlist.stream().forEach((contentkeyword) -> {
                listcontentmap.add(cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref()));
            });
            contentmap.put("keywords", listcontentmap);
        }
        
        return contentmap;
    }
}
