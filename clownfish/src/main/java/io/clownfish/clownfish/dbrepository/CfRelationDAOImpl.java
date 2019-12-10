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

import io.clownfish.clownfish.daointerface.CfRelationDAO;
import io.clownfish.clownfish.dbentities.CfRelation;
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
public class CfRelationDAOImpl implements CfRelationDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfRelationDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfRelation findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelation.findById");  
        query.setParameter("id", id);
        CfRelation cfrelation = (CfRelation) query.getSingleResult();
        return cfrelation;
    }

    @Override
    public boolean create(CfRelation entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfRelation entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfRelation entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfRelation> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelation.findAll");
        List<CfRelation> cfrelationlist = query.getResultList();
        return cfrelationlist;
    }

    @Override
    public CfRelation findByRef1(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelation.findByRef1");  
        query.setParameter("ref1", ref);
        CfRelation cfrelation = (CfRelation) query.getSingleResult();
        return cfrelation;
    }

    @Override
    public CfRelation findByRef2(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfRelation.findByRef2");  
        query.setParameter("ref2", ref);
        CfRelation cfrelation = (CfRelation) query.getSingleResult();
        return cfrelation;
    }
}
