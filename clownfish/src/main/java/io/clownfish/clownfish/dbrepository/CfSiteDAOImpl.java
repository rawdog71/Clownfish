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

import io.clownfish.clownfish.daointerface.CfSiteDAO;
import io.clownfish.clownfish.dbentities.CfSite;
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
public class CfSiteDAOImpl implements CfSiteDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSiteDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfSite> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findAll");
        List<CfSite> cfsitelist = query.getResultList();
        return cfsitelist;
    }

    @Override
    public CfSite findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findByName");  
        query.setParameter("name", name);
        CfSite cfsite = (CfSite) query.getSingleResult();
        return cfsite;
    }

    @Override
    public CfSite findByTemplateref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findByTemplateref");  
        query.setParameter("templateref", ref);
        CfSite cfsite = (CfSite) query.getSingleResult();
        return cfsite;
    }
    
    @Override
    public CfSite findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findById");  
        query.setParameter("id", id);
        CfSite cfsite = (CfSite) query.getSingleResult();
        return cfsite;
    }
    
    @Override
    public CfSite create(CfSite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfSite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfSite edit(CfSite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfSite> findByParentref(CfSite ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = null;
        if (null == ref) {
            query = (TypedQuery) session.getNamedQuery("CfSite.findByParentrefNull");
        } else {
            query = (TypedQuery) session.getNamedQuery("CfSite.findByParentref");
            query.setParameter("parentref", ref);
        }
        List<CfSite> cfsitelist = query.getResultList();
        return cfsitelist;
    }

    @Override
    public CfSite findByAliaspath(String alias) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findByAliaspath");  
        query.setParameter("aliaspath", alias);
        CfSite cfsite = (CfSite) query.getSingleResult();
        return cfsite;
    }

    @Override
    public List<CfSite> findBySitemap(boolean sitemap) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findBySitemap");  
        query.setParameter("sitemap", sitemap);
        List<CfSite> cfsitelist = query.getResultList();
        return cfsitelist;
    }

    @Override
    public CfSite findByShorturl(String shorturl) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSite.findByShorturl");  
        query.setParameter("shorturl", shorturl);
        CfSite cfsite = (CfSite) query.getSingleResult();
        return cfsite;
    }
}
