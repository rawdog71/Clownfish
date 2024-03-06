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
package io.clownfish.clownfish.dbrepository;

import io.clownfish.clownfish.daointerface.CfLayoutcontentDAO;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import java.util.List;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sulzbachr
 */
@Repository
public class CfLayoutcontentDAOImpl implements CfLayoutcontentDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfLayoutcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfLayoutcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfLayoutcontent.findAll");
        List<CfLayoutcontent> cflayoutcontentlist = query.getResultList();
        return cflayoutcontentlist;
    }
    
    @Override
    public List<CfLayoutcontent> findBySiteref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfLayoutcontent.findBySiteref");  
        query.setParameter("siteref", ref);
        List<CfLayoutcontent> cflayoutcontentlist = query.getResultList();
        return cflayoutcontentlist;
    }

    @Override
    public CfLayoutcontent create(CfLayoutcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfLayoutcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfLayoutcontent edit(CfLayoutcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfLayoutcontent> findBySiterefAndTemplateref(Long siteref, Long templateref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfLayoutcontent.findBySiterefAndTemplateref");  
        query.setParameter("siteref", siteref);
        query.setParameter("templateref", templateref);
        List<CfLayoutcontent> cflayoutcontentlist = query.getResultList();
        return cflayoutcontentlist;
    }

    @Override
    public List<CfLayoutcontent> findBySiterefAndTemplaterefAndContenttype(Long siteref, Long templateref, String contenttype) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfLayoutcontent.findBySiterefAndTemplaterefAndContenttype");  
        query.setParameter("siteref", siteref);
        query.setParameter("templateref", templateref);
        query.setParameter("contenttype", contenttype);
        List<CfLayoutcontent> cflayoutcontentlist = query.getResultList();
        return cflayoutcontentlist;
    }

    @Override
    public CfLayoutcontent findBySiterefAndTemplaterefAndContenttypeAndLfdnr(Long siteref, Long templateref, String contenttype, int lfdnr) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfLayoutcontent.findBySiterefAndTemplaterefAndContenttypeAndLfdnr");  
        query.setParameter("siteref", siteref);
        query.setParameter("templateref", templateref);
        query.setParameter("contenttype", contenttype);
        query.setParameter("lfdnr", lfdnr);
        try {
            CfLayoutcontent cflayoutcontentlist = (CfLayoutcontent) query.getSingleResult();
            return cflayoutcontentlist;
        } catch (Exception ex) {
            return null;
        }
    }

}
