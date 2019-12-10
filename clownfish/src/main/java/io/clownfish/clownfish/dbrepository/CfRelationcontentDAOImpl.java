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

import io.clownfish.clownfish.daointerface.CfRelationcontentDAO;
import io.clownfish.clownfish.dbentities.CfRelation;
import io.clownfish.clownfish.dbentities.CfRelationcontent;
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
public class CfRelationcontentDAOImpl implements CfRelationcontentDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfRelationcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfRelationcontent findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelationcontent.findById");  
        query.setParameter("id", id);
        CfRelationcontent cfrelationcontent = (CfRelationcontent) query.getSingleResult();
        return cfrelationcontent;
    }

    @Override
    public boolean create(CfRelationcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfRelationcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfRelationcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfRelationcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelationcontent.findAll");
        List<CfRelationcontent> cfrelationcontentlist = query.getResultList();
        return cfrelationcontentlist;
    }

    @Override
    public CfRelationcontent findByRelationref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelationcontent.findByRelationref");  
        query.setParameter("relationref", ref);
        CfRelationcontent cfrelationcontent = (CfRelationcontent) query.getSingleResult();
        return cfrelationcontent;
    }

    @Override
    public CfRelationcontent findByRefcontent1(Long refcontent) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelationcontent.findByContent1ref");  
        query.setParameter("content1ref", refcontent);
        CfRelationcontent cfrelationcontent = (CfRelationcontent) query.getSingleResult();
        return cfrelationcontent;
    }

    @Override
    public CfRelationcontent findByRefcontent2(Long refcontent) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelationcontent.findByContent2ref");  
        query.setParameter("content2ref", refcontent);
        CfRelationcontent cfrelationcontent = (CfRelationcontent) query.getSingleResult();
        return cfrelationcontent;
    }

}
