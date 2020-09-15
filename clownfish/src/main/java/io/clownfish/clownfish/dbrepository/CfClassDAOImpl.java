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

import io.clownfish.clownfish.daointerface.CfClassDAO;
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
public class CfClassDAOImpl implements CfClassDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfClassDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfClass findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfClass.findById");  
        query.setParameter("id", id);
        CfClass cfclass = (CfClass) query.getSingleResult();
        return cfclass;
    }

    @Override
    public CfClass create(CfClass entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfClass entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfClass edit(CfClass entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfClass> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfClass.findAll");  
        List<CfClass> cfclasslist = query.getResultList();
        return cfclasslist;
    }

    @Override
    public CfClass findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfClass.findByName");  
        query.setParameter("name", name);
        CfClass cfclass = (CfClass) query.getSingleResult();
        return cfclass;
    }
}
