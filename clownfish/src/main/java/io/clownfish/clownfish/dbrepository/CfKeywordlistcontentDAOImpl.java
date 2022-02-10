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

import io.clownfish.clownfish.daointerface.CfKeywordlistcontentDAO;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
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
public class CfKeywordlistcontentDAOImpl implements CfKeywordlistcontentDAO {
    private final SessionFactory sessionFactory;
    
    @Autowired
    public CfKeywordlistcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfKeywordlistcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeywordlistcontent.findAll");
        List<CfKeywordlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfKeywordlistcontent> findByKeywordlistref(long keywordlistref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeywordlistcontent.findByKeywordlistref");
        query.setParameter("keywordlistref", keywordlistref);
        List<CfKeywordlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfKeywordlistcontent> findByKeywordref(long keywordref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeywordlistcontent.findByKeywordref");
        query.setParameter("keywordref", keywordref);
        List<CfKeywordlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }
    
    @Override
    public CfKeywordlistcontent findByKeywordrefAndKeywordlistref(long keywordref, long keywordlistref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeywordlistcontent.findByKeywordrefAndKeywordlistref");
        query.setParameter("keywordref", keywordref);
        query.setParameter("keywordlistref", keywordlistref);
        CfKeywordlistcontent cfcontent = (CfKeywordlistcontent) query.getSingleResult();
        return cfcontent;
    }
    
    @Override
    public CfKeywordlistcontent create(CfKeywordlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfKeywordlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfKeywordlistcontent edit(CfKeywordlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }
}
