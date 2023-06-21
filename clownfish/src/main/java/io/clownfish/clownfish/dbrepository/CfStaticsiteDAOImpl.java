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

import io.clownfish.clownfish.daointerface.CfStaticsiteDAO;
import io.clownfish.clownfish.dbentities.CfStaticsite;
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
public class CfStaticsiteDAOImpl implements CfStaticsiteDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfStaticsiteDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfStaticsite> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStaticsite.findAll");
        List<CfStaticsite> cfstaticsitelist = query.getResultList();
        return cfstaticsitelist;
    }

    @Override
    public List<CfStaticsite> findBySite(String site) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStaticsite.findBySite");  
        query.setParameter("site", site);
        List<CfStaticsite> cfstaticsitelist = query.getResultList();
        return cfstaticsitelist;
    }

    @Override
    public CfStaticsite findByUrlparams(String urlparam) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStaticsite.findByUrlparams");  
        query.setParameter("urlparams", urlparam);
        CfStaticsite cfsite = (CfStaticsite) query.getSingleResult();
        return cfsite;
    }
    
    @Override
    public CfStaticsite findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStaticsite.findById");  
        query.setParameter("id", id);
        CfStaticsite cfsite = (CfStaticsite) query.getSingleResult();
        return cfsite;
    }
    
    @Override
    public CfStaticsite create(CfStaticsite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfStaticsite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfStaticsite edit(CfStaticsite entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public CfStaticsite findBySiteAndUrlparams(String name, String urlparams) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStaticsite.findBySiteAndUrlparams");
        query.setParameter("site", name);
        query.setParameter("urlparams", urlparams);
        CfStaticsite cfsite = (CfStaticsite) query.getSingleResult();
        return cfsite;
    }
}
