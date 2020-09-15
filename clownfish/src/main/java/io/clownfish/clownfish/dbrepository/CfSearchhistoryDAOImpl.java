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

import io.clownfish.clownfish.daointerface.CfSearchhistoryDAO;
import io.clownfish.clownfish.dbentities.CfSearchhistory;
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
public class CfSearchhistoryDAOImpl implements CfSearchhistoryDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSearchhistoryDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfSearchhistory findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchhistory.findById");  
        query.setParameter("id", id);
        CfSearchhistory cfsearchhistory = (CfSearchhistory) query.getSingleResult();
        return cfsearchhistory;
    }

    @Override
    public CfSearchhistory findByExpression(String expression) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchhistory.findByExpression");  
        query.setParameter("expression", expression);
        CfSearchhistory cfsearchhistory = (CfSearchhistory) query.getSingleResult();
        return cfsearchhistory;
    }

    @Override
    public List<CfSearchhistory> findByExpressionBeginning(String expression) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchhistory.findByExpressionBeginning");
        query.setParameter("expression", expression + "%");
        List<CfSearchhistory> cfsearchhistorylist = query.getResultList();
        return cfsearchhistorylist;
    }

    @Override
    public CfSearchhistory create(CfSearchhistory entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfSearchhistory entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfSearchhistory edit(CfSearchhistory entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfSearchhistory> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchhistory.findAll");
        List<CfSearchhistory> cfsearchhistorylist = query.getResultList();
        return cfsearchhistorylist;
    }

}
