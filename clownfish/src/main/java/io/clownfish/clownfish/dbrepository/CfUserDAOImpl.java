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

import io.clownfish.clownfish.daointerface.CfUserDAO;
import io.clownfish.clownfish.dbentities.CfUser;
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
public class CfUserDAOImpl implements CfUserDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfUserDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfUser findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUser.findById");  
        query.setParameter("id", id);
        try {
            CfUser cfuser = (CfUser) query.getSingleResult();
            return cfuser;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfUser findByEmail(String email) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUser.findByEmail");  
        query.setParameter("email", email);
        try {
            CfUser cfuser = (CfUser) query.getSingleResult();
            return cfuser;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfUser create(CfUser entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfUser entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfUser edit(CfUser entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfUser> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUser.findAll");
        List<CfUser> cfuserlist = query.getResultList();
        return cfuserlist;
    }
    
}
