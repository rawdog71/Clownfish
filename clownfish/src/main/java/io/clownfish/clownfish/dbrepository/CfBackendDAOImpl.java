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

import io.clownfish.clownfish.daointerface.CfBackendDAO;
import io.clownfish.clownfish.dbentities.CfBackend;
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
public class CfBackendDAOImpl implements CfBackendDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfBackendDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfBackend findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfBackend.findById");  
        query.setParameter("id", id);
        try {
            CfBackend cfbackend = (CfBackend) query.getSingleResult();
            return cfbackend;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfBackend create(CfBackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfBackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfBackend edit(CfBackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfBackend> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfBackend.findAll");
        List<CfBackend> cfbackendlist = query.getResultList();
        return cfbackendlist;
    }

    @Override
    public CfBackend findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfBackend.findByName");  
        query.setParameter("name", name);
        try {
            CfBackend cfbackend = (CfBackend) query.getSingleResult();
            return cfbackend;
        } catch (Exception ex) {
            return null;
        }
    }
}
