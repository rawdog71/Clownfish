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

import io.clownfish.clownfish.daointerface.CfAttributDAO;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
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
public class CfAttributDAOImpl implements CfAttributDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfAttributDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfAttribut findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttribut.findById");  
        query.setParameter("id", id);
        CfAttribut cfattribut = (CfAttribut) query.getSingleResult();
        return cfattribut;
    }

    @Override
    public CfAttribut create(CfAttribut entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAttribut entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAttribut edit(CfAttribut entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfAttribut> findByClassref(CfClass classref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttribut.findByClassref");
        query.setParameter("classref", classref);
        List<CfAttribut> cfattributlist = query.getResultList();
        return cfattributlist;
    }

    @Override
    public CfAttribut findByNameAndClassref(String name, CfClass classref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttribut.findByNameAndClassref");  
        query.setParameter("name", name);
        query.setParameter("classref", classref);
        CfAttribut cfattribut = (CfAttribut) query.getSingleResult();
        return cfattribut;
    }
}
