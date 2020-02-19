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

import io.clownfish.clownfish.daointerface.CfUserBackendDAO;
import io.clownfish.clownfish.dbentities.CfUserbackend;
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
public class CfUserBackendDAOImpl implements CfUserBackendDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfUserBackendDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    

    @Override
    public boolean create(CfUserbackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfUserbackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfUserbackend entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfUserbackend> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUserbackend.findAll");
        List<CfUserbackend> cfuserbackendlist = query.getResultList();
        return cfuserbackendlist;
    }

    @Override
    public List<CfUserbackend> findByUserRef(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUserbackend.findByUserref");
        query.setParameter("userref", id);
        List<CfUserbackend> cfuserbackendlist = query.getResultList();
        return cfuserbackendlist;
    }

    @Override
    public List<CfUserbackend> findByBackendRef(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfUserbackend.findByBackendref");
        query.setParameter("backendref", id);
        List<CfUserbackend> cfuserbackendlist = query.getResultList();
        return cfuserbackendlist;
    }
}
