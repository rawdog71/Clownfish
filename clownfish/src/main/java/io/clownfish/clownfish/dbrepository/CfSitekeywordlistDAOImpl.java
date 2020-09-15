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

import io.clownfish.clownfish.daointerface.CfSitekeywordlistDAO;
import io.clownfish.clownfish.dbentities.CfSitekeywordlist;
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
public class CfSitekeywordlistDAOImpl implements CfSitekeywordlistDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSitekeywordlistDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfSitekeywordlist> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitekeywordlist.findAll");
        List<CfSitekeywordlist> cfsitekeywordlist = query.getResultList();
        return cfsitekeywordlist;
    }
    
    @Override
    public List<CfSitekeywordlist> findBySiteref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitekeywordlist.findBySiteref");  
        query.setParameter("siteref", ref);
        List<CfSitekeywordlist> cfsitekeywordlist = query.getResultList();
        return cfsitekeywordlist;
    }

    @Override
    public CfSitekeywordlist create(CfSitekeywordlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfSitekeywordlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfSitekeywordlist edit(CfSitekeywordlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfSitekeywordlist> findByKeywordlistref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitekeywordlist.findByKeywordlistref");
        query.setParameter("keywordlistref", ref);
        List<CfSitekeywordlist> cfsitekeywordlist = query.getResultList();
        return cfsitekeywordlist;
    }
}
