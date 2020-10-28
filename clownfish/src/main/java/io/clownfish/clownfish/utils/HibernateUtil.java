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

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import java.io.ByteArrayInputStream;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Component
public class HibernateUtil {

    @Autowired CfClassService cfclassservice;
    @Autowired CfAttributService cfattributservice;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfListcontentService cflistcontentService;
    private @Getter @Setter HashMap<String, Session> classsessions = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);
    
    public void generateTablesDatamodel(int initHibernate) {
        //classsessions = new HashMap<>();
        List<CfClass> classlist = cfclassservice.findAll();
        for (CfClass clazz : classlist) {
            Document xmldoc = DocumentHelper.createDocument();
            xmldoc.setName(clazz.getName());
            Element root = xmldoc.addElement("hibernate-mapping");
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
            Session session = sessionFactory.openSession();
            classsessions.put(clazz.getName(), session);
            if (initHibernate > 0) {
                Transaction tx = session.beginTransaction();
                fillTable(clazz.getName(), session);
                tx.commit();
            }
            session.close();
        }

        LOGGER.info("Datamodel created");
    }

    private void fillTable(String classname, Session session) {
        CfClass cfclass = cfclassservice.findByName(classname);
        List<CfClasscontent> classcontentlist = cfclasscontentService.findByClassref(cfclass);

        for (CfClasscontent classcontent : classcontentlist) {
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
            Map entity = fillEntity(new HashMap(), classcontent, attributcontentlist);
            try {
                session.save(classname, entity);
            } catch (org.hibernate.id.IdentifierGenerationException ex) {
                LOGGER.warn("NOT SAVED:" + classcontent.getName());
            }
            //LOGGER.info(entity.toString());
        }
    }
    
    public void insertContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session = classsessions.get(classname).getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        Map entity = fillEntity(new HashMap(), classcontent, attributcontentlist);
        try {
            session.save(classname, entity);
            tx.commit();
            session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        }
    }
    
    public void updateContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session = classsessions.get(classname).getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        
        Map entity = (Map) session.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        entity = fillEntity(entity, classcontent, attributcontentlist);
        try {
            session.update(classname, entity);
            tx.commit();
            session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        }
    }
    
    public void deleteContent(CfClasscontent classcontent) {
        String classname = classcontent.getClassref().getName();
        Session session = classsessions.get(classname).getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        
        Map entity = (Map) session.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
        entity = fillEntity(entity, classcontent, attributcontentlist);
        try {
            session.delete(classname, entity);
            tx.commit();
            session.close();
        } catch (org.hibernate.id.IdentifierGenerationException ex) {
            LOGGER.warn("NOT SAVED:" + classcontent.getName());
        }
    }

    private String getHibernateType(String clownfishtype) {
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
    
    private int hasIdentifier(List<CfAttribut> attributlist) {
        int count = 0;
        for (CfAttribut attribut : attributlist) {
            if (attribut.getIdentity()) {
                count += 1;
            }
        }
        return count;
    }
    
    private void makePrimaryKey(List<CfAttribut> attributlist, Element elementclass, int idcount) {
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
    
    public void generateRelationsDatamodel(int initHibernate) {
        List<CfClass> classlist = cfclassservice.findAll();
        for (CfClass clazz : classlist) {
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                switch (attribut.getAttributetype().getName()) {
                    case "classref":
                        Document xmldoc = DocumentHelper.createDocument();
                        xmldoc.setName(clazz.getName());
                        Element root = xmldoc.addElement("hibernate-mapping");
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

                        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
                        SessionFactory sessionFactory = new MetadataSources(standardRegistry).addInputStream(new ByteArrayInputStream(xmldoc.asXML().getBytes())).buildMetadata().buildSessionFactory();
                        Session session = sessionFactory.openSession();
                        Session session_class = classsessions.get(clazz.getName()).getSessionFactory().openSession();
                        Session session_attribut = classsessions.get(attribut.getRelationref().getName()).getSessionFactory().openSession();
                        classsessions.put(clazz.getName() + "_" + attribut.getName(), session);
                        if (initHibernate > 0) {
                            Transaction tx = session.beginTransaction();                        
                            fillRelation(clazz.getName(), attribut.getName(), session, session_class, session_attribut);
                            tx.commit();
                        }
                        session_attribut.close();
                        session_class.close();
                        session.close();
                        break;
                    case "assetref":
                        break;            
                }
            }
        }
    }
    
    private void fillRelation(String classname, String attributname, Session session, Session session_class, Session session_referenz) {
        CfClass cfclass = cfclassservice.findByName(classname);
        List<CfClasscontent> classcontentlist = cfclasscontentService.findByClassref(cfclass);

        for (CfClasscontent classcontent : classcontentlist) {
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(classcontent);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                if (0 == attributcontent.getAttributref().getName().compareToIgnoreCase(attributname)) {
                    CfList contentclassref = attributcontent.getClasscontentlistref();
                    if (null != contentclassref) {
                        //System.out.println(contentclassref.getName());
                        List<CfListcontent> listcontentlist = cflistcontentService.findByListref(contentclassref.getId());
                        for (CfListcontent listcontent : listcontentlist) {
                            Map content = (Map) session_class.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + classcontent.getId()).getSingleResult();
                            Map referenz = (Map) session_referenz.createQuery("FROM " + attributcontent.getAttributref().getRelationref().getName() + " c WHERE c.cf_contentref = " + listcontent.getCfListcontentPK().getClasscontentref()).getSingleResult();
                            Map entity = new HashMap();
                            Map id = new HashMap();
                            id.put(classname + "_ref", classcontent.getId());
                            id.put(attributname + "_ref", listcontent.getCfListcontentPK().getClasscontentref());
                            entity.put("id_", id);
                            entity.put(classname + "_usr_ref", content.get("cf_id"));
                            entity.put(attributname + "_usr_ref", referenz.get("cf_id"));
                            session.save(classname + "_" + attributname, entity);
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
            Session session_referenz = classsessions.get(referenzname).getSessionFactory().openSession();        
            if (null != list) {
                List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByContentclassRef(list);
                for (CfAttributcontent attributcontent : attributcontentlist) {
                    String classname = attributcontent.getClasscontentref().getClassref().getName();
                    String attributname = attributcontent.getAttributref().getName();
                    String refname = attributcontent.getClasscontentref().getClassref().getName() + "_" + attributname;
                    Session session = classsessions.get(refname).getSessionFactory().openSession();
                    Session session_class = classsessions.get(classname).getSessionFactory().openSession();
                    Query q = session.createQuery("DELETE FROM " + refname + " WHERE " + classname + "_ref_ = " + attributcontent.getClasscontentref().getId());
                    Transaction tx = session.beginTransaction();
                    int count = q.executeUpdate();

                    List<CfListcontent> listcontentlist = cflistcontentService.findByListref(list.getId());
                    for (CfListcontent listcontent : listcontentlist) {
                        Map content = (Map) session_class.createQuery("FROM " + classname + " c WHERE c.cf_contentref = " + attributcontent.getClasscontentref().getId()).getSingleResult();
                        Map referenz = (Map) session_referenz.createQuery("FROM " + attributcontent.getAttributref().getRelationref().getName() + " c WHERE c.cf_contentref = " + listcontent.getCfListcontentPK().getClasscontentref()).getSingleResult();
                        Map entity = new HashMap();
                        Map id = new HashMap();
                        id.put(classname + "_ref", attributcontent.getClasscontentref().getId());
                        id.put(attributname + "_ref", listcontent.getCfListcontentPK().getClasscontentref());
                        entity.put("id_", id);
                        entity.put(classname + "_usr_ref", content.get("cf_id"));
                        entity.put(attributname + "_usr_ref", referenz.get("cf_id"));
                        session.save(classname + "_" + attributname, entity);
                    }                        
                    tx.commit();
                    session.close();
                    session_class.close();
                }
            }
            session_referenz.close();
        }
    }
    
    public void deleteRelation(CfList list, CfClasscontent classcontent) { 
        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByContentclassRef(list);
        for (CfAttributcontent attributcontent : attributcontentlist) {
            if (attributcontent.getClasscontentref().getId() == classcontent.getId()) {
                String classname = attributcontent.getClasscontentref().getClassref().getName();
                String attributname = attributcontent.getAttributref().getName();
                String refname = attributcontent.getClasscontentref().getClassref().getName() + "_" + attributname;
                Session session = classsessions.get(refname).getSessionFactory().openSession();
                Query q = session.createQuery("DELETE FROM " + refname + " WHERE " + classname + "_ref_ = " + attributcontent.getClasscontentref().getId());
                Transaction tx = session.beginTransaction();
                int count = q.executeUpdate();                     
                tx.commit();
                session.close();
            }
        }
    }
    
    private Map fillEntity(Map entity, CfClasscontent classcontent, List<CfAttributcontent> attributcontentlist) {
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
}
