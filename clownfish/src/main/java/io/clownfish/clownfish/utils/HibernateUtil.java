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
import io.clownfish.clownfish.datamodels.HibernateInit;
import io.clownfish.clownfish.datamodels.SearchValues;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.NoResultException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Component
public class HibernateUtil implements Runnable {
    private static CfClassService cfclassservice;
    private static CfAttributService cfattributservice;
    private static CfClasscontentService cfclasscontentService;
    private static CfAttributcontentService cfattributcontentService;
    private static CfListcontentService cflistcontentService;
    private static CfKeywordService cfkeywordService;
    private static CfClasscontentKeywordService cfclasscontentkeywordService;
    private static @Getter @Setter HashMap<String, Session> classsessions = new HashMap<>();
    private static ServiceStatus serviceStatus;
    private static String datasourceURL;
    @Autowired MarkdownUtil markdownUtil;
    private @Getter @Setter int hibernateInit = 0;
    @Autowired private PropertyUtil propertyUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);
    
    public void init(HibernateInit hibernateInit) {
        this.cfclassservice = hibernateInit.getCfclassservice();
        this.cfattributservice = hibernateInit.getCfattributservice();
        this.cfclasscontentService = hibernateInit.getCfclasscontentService();
        this.cfattributcontentService = hibernateInit.getCfattributcontentService();
        this.cflistcontentService = hibernateInit.getCflistcontentService();
        this.serviceStatus = hibernateInit.getServiceStatus();
        this.cfkeywordService = hibernateInit.getCfkeywordService();
        this.cfclasscontentkeywordService = hibernateInit.getCfclasscontentkeywordService();
        this.datasourceURL = hibernateInit.getDatasourceURL();
    }
    
    public static synchronized void generateTablesDatamodel(int initHibernate) {
        try {
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
                        elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName(), attribut.getRelationtype()));
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
            //System.out.println(xmldoc.asXML());
            ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().applySetting("hibernate.connection.url", datasourceURL).build();
            SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes("UTF-8"))).buildMetadata().buildSessionFactory();
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
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(HibernateUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static synchronized void generateTablesDatamodel(String classname, int initHibernate) {
        try {
            serviceStatus.setMessage("Regenerating dynamic tables");
            serviceStatus.setOnline(false);
            Session session_tables_drop = classsessions.get("tables").getSessionFactory().openSession();
            try {
                Query q = session_tables_drop.createSQLQuery("DROP TABLE usr_" + classname);
                Transaction tx = session_tables_drop.beginTransaction();
                int count = q.executeUpdate();
                tx.commit();
            } catch (Exception ex) {
                LOGGER.warn("DROP TABLE " + classname + " NOT POSSIBLE.");
            }
            session_tables_drop.close();
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
                        elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName(), attribut.getRelationtype()));
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
            //System.out.println(xmldoc.asXML());
            ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().applySetting("hibernate.connection.url", datasourceURL).build();
            SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes("UTF-8"))).buildMetadata().buildSessionFactory();
            Session session_tables = sessionFactory.openSession();
            classsessions.put("tables", session_tables);
            for (CfClass clazz : classlist) {
                if ((initHibernate > 0) && (0 == clazz.getName().compareToIgnoreCase(classname))) {
                    fillTable(clazz.getName(), session_tables);
                }
            }
            session_tables.close();
            
            LOGGER.info("Data Model recreated");
            serviceStatus.setMessage("online");
            serviceStatus.setOnline(true);
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(HibernateUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void fillTable(String classname, Session session) {
        CfClass cfclass = cfclassservice.findByName(classname);
        List<CfClasscontent> classcontentlist = cfclasscontentService.findByClassref(cfclass);
        Query q = session.createSQLQuery("TRUNCATE TABLE usr_" + classname);
        Transaction txt = session.beginTransaction();
        int count = q.executeUpdate();
        txt.commit();
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

    private static String getHibernateType(String clownfishtype, int relationtype) {
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
                if (0 == relationtype) {
                    return "string";
                } else {
                    return "long";
                }
            case "assetref":
                return "string";
            default:
                return null;
        }
    }
    
    private static String getClownfishType(String tablename, String fieldname) {
        CfClass myclass = cfclassservice.findByName(tablename);
        CfAttribut myattribute = cfattributservice.findByNameAndClassref(fieldname, myclass);
        
        return myattribute.getAttributetype().getName();
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
                    elementproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName(), attribut.getRelationtype()));
                    elementproperty.addAttribute("index", "idx_" + attribut.getName());
                }
            }
        } else {
            for (CfAttribut attribut : attributlist) {
                if (attribut.getIdentity()) {
                    Element elementkeyproperty = elementclass.addElement("property");
                    elementkeyproperty.addAttribute("name", attribut.getName());
                    elementkeyproperty.addAttribute("column", attribut.getName() + "_");
                    elementkeyproperty.addAttribute("type", getHibernateType(attribut.getAttributetype().getName(), attribut.getRelationtype()));
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
        //System.out.println(xmldoc.asXML());
        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().applySetting("hibernate.connection.url", datasourceURL).build();
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

        Query q = session_relations.createSQLQuery("TRUNCATE TABLE usr_rel_" + classname + "_" + attributname);
        Transaction txt = session_relations.beginTransaction();
        int count = q.executeUpdate();
        txt.commit();
        
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
                    if (0 == attributcontent.getAttributref().getRelationtype()) {
                        if (null != attributcontent.getClasscontentlistref()) {
                            entity.put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentlistref().getName());
                        } else {
                            entity.put(attributcontent.getAttributref().getName(), null);
                        }
                    } else {
                        if (null != attributcontent.getContentInteger()) {
                            entity.put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().longValue());
                        } else {
                            entity.put(attributcontent.getAttributref().getName(), null);
                        }
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
        Map contentmap = null;
        Map outputmap = new HashMap();
        Session session_tables = classsessions.get("tables").getSessionFactory().openSession();
        Query query = null;
        query = session_tables.createQuery("FROM " + tablename + " c WHERE cf_contentref = " + contentid);
        try {
            contentmap = (Map) query.getSingleResult();
            contentmap.forEach(
                    (k, v) -> 
                        {
                            if ((!k.toString().startsWith("cf_")) && (0 != k.toString().compareToIgnoreCase("$type$"))) {
                                if (0 == getClownfishType(tablename, k.toString()).compareToIgnoreCase("markdown")) {
                                    markdownUtil.initOptions();
                                    if (null != v) {
                                        v = markdownUtil.parseMarkdown(v.toString(), markdownUtil.getMarkdownOptions());
                                    } else {
                                        v = markdownUtil.parseMarkdown("", markdownUtil.getMarkdownOptions());
                                    }
                                }
                            }
                            outputmap.put(k, v);
                        }
            );
            session_tables.close();
            /* add keywords  */
            List<CfClasscontentkeyword> contentkeywordlist;
            contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(contentid);
            if (!contentkeywordlist.isEmpty()) {
                ArrayList listcontentmap = new ArrayList();
                contentkeywordlist.stream().forEach((contentkeyword) -> {
                    listcontentmap.add(cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref()));
                });
                outputmap.put("keywords", listcontentmap);
                return outputmap;
            }
        } catch (NoResultException ex) {
            LOGGER.error("HIBERNATEUTIL: " + tablename + " is empty. Please use hibernate.init=1 in application.properties");
        }
        return outputmap;
    }
    
    public Map getContent(String tablename, long contentid, String tablename_rel, long contentid_rel) {
        Map contentmap = null;
        Map outputmap = new HashMap();
        Session session_relations = classsessions.get("relations").getSessionFactory().openSession();
        Query query = null;
        query = session_relations.createQuery("FROM " + tablename + "_" + tablename_rel + "c WHERE " + tablename + "_ref_ = " + contentid + " AND " + tablename_rel + "_ref_ = " + contentid_rel);
        try {
            contentmap = (Map) query.getSingleResult();
            contentmap.forEach(
                    (k, v) -> 
                        {
                            if ((!k.toString().startsWith("cf_")) && (0 != k.toString().compareToIgnoreCase("$type$"))) {
                                if (0 == getClownfishType(tablename, k.toString()).compareToIgnoreCase("markdown")) {
                                    markdownUtil.initOptions();
                                    if (null != v) {
                                        v = markdownUtil.parseMarkdown(v.toString(), markdownUtil.getMarkdownOptions());
                                    } else {
                                        v = markdownUtil.parseMarkdown("", markdownUtil.getMarkdownOptions());
                                    }
                                }
                            }
                            outputmap.put(k, v);
                        }
            );
            session_relations.close();
            /* add keywords  */
            List<CfClasscontentkeyword> contentkeywordlist;
            contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(contentid);
            if (!contentkeywordlist.isEmpty()) {
                ArrayList listcontentmap = new ArrayList();
                contentkeywordlist.stream().forEach((contentkeyword) -> {
                    listcontentmap.add(cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref()));
                });
                outputmap.put("keywords", listcontentmap);
                return outputmap;
            }
        } catch (NoResultException ex) {
            LOGGER.error("HIBERNATEUTIL: " + tablename + " is empty. Please use hibernate.init=1 in application.properties");
        }
        return outputmap;
    }
    
    private SearchValues getSearchValues(String searchvalue) {
        String comparator = "eq";
        // contains
        if (searchvalue.startsWith(":co:")) {
            comparator = "co";
            searchvalue = searchvalue.substring(4);
        }
        // equals
        if (searchvalue.startsWith(":eq:")) {
            comparator = "eq";
            searchvalue = searchvalue.substring(4);
        }
        // ends with
        if (searchvalue.startsWith(":ew:")) {
            comparator = "ew";
            searchvalue = searchvalue.substring(4);
        }
        // starts with
        if (searchvalue.startsWith(":sw:")) {
            comparator = "sw";
            searchvalue = searchvalue.substring(4);
        }
        // not equals
        if (searchvalue.startsWith(":ne:")) {
            comparator = "ne";
            searchvalue = searchvalue.substring(4);
        }
        // greater than
        if (searchvalue.startsWith(":gt:")) {
            comparator = "gt";
            searchvalue = searchvalue.substring(4);
        }
        // less than
        if (searchvalue.startsWith(":lt:")) {
            comparator = "lt";
            searchvalue = searchvalue.substring(4);
        }
        // greater than or equal
        if (searchvalue.startsWith(":gte:")) {
            comparator = "gte";
            searchvalue = searchvalue.substring(4);
        }
        // less than or equal
        if (searchvalue.startsWith(":lte:")) {
            comparator = "lte";
            searchvalue = searchvalue.substring(4);
        }
        searchvalue = searchvalue.toLowerCase();
        return new SearchValues(comparator, searchvalue);
    }
    
    public Query getQuery(Session session_tables, HashMap<String, String> searchmap, String inst_klasse) {
        Query query = null;
        if (!searchmap.isEmpty()) {
            CfClass clazz = cfclassservice.findByName(inst_klasse);
            String whereclause = " WHERE ";
            for (String searchcontent : searchmap.keySet()) {
                String searchcontentval = searchcontent.substring(0, searchcontent.length()-2);
                String searchvalue = searchmap.get(searchcontent);
                if (clazz.isEncrypted()) {
                    List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
                    for (CfAttribut attribut : attributlist) {
                        if (((isEncryptable(attribut)) && (!attribut.getIdentity())) && (0 == attribut.getName().compareToIgnoreCase(searchcontentval))) {
                            searchvalue = EncryptUtil.encrypt(searchvalue, propertyUtil.getPropertyValue("aes_key"));
                        }
                    }
                }
                SearchValues sv = getSearchValues(searchvalue);
                switch (sv.getComparartor()) {
                    case "eq":
                        whereclause += searchcontentval + " = '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "sw":
                        whereclause += searchcontentval + " LIKE '" + sv.getSearchvalue() + "%' AND ";
                        break;
                    case "ew":
                        whereclause += searchcontentval + " LIKE '%" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "co":
                        whereclause += searchcontentval + " LIKE '%" + sv.getSearchvalue() + "%' AND ";
                        break;
                    case "gt":
                        whereclause += searchcontentval + " > '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "lt":
                        whereclause += searchcontentval + " < '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "gte":
                        whereclause += searchcontentval + " >= '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "lte":
                        whereclause += searchcontentval + " <= '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "ne":
                        whereclause += searchcontentval + " <> '" + sv.getSearchvalue() + "' AND ";
                        break;
                }

            }
            whereclause = whereclause.substring(0, whereclause.length()-5);
            query = session_tables.createQuery("FROM " + inst_klasse + " c " + whereclause);
        } else {
            query = session_tables.createQuery("FROM " + inst_klasse + " c ");
        }
        return query;
    }
    
    private boolean isEncryptable(CfAttribut attribut) {
        switch (attribut.getAttributetype().getName()) {
            case "string":
            case "text":
            case "htmltext":
            case "markdown":
                return true;
            default:
                return false;
        }
    }

    @Override
    public void run() {
        generateTablesDatamodel(hibernateInit);
        // generate Relation Tables
        generateRelationsDatamodel(hibernateInit);
    }
}
