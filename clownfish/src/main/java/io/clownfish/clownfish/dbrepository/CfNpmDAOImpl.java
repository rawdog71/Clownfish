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

import io.clownfish.clownfish.daointerface.CfNpmDAO;
import io.clownfish.clownfish.dbentities.CfNpm;
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
public class CfNpmDAOImpl implements CfNpmDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfNpmDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfNpm findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfNpm.findById");  
        query.setParameter("id", id);
        try {
            CfNpm cfmaven = (CfNpm) query.getSingleResult();
            return cfmaven;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfNpm create(CfNpm entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfNpm entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfNpm edit(CfNpm entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfNpm> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfNpm.findAll");
        List<CfNpm> cfmavenlist = query.getResultList();
        return cfmavenlist;
    }

    @Override
    public CfNpm findByNpmId(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfNpm.findByNpmId");  
        query.setParameter("npmId", name);
        try {
            CfNpm cfmaven = (CfNpm) query.getSingleResult();
            return cfmaven;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfNpm findByNpmIdAndNpmLatestversion(String name, String version) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfNpm.findByNpmIdAndNpmLatestversion");  
        query.setParameter("npmId", name);
        query.setParameter("npmLatestversion", version);
        try {
            CfNpm cfmaven = (CfNpm) query.getSingleResult();
            return cfmaven;
        } catch (Exception ex) {
            return null;
        }
    }
}
