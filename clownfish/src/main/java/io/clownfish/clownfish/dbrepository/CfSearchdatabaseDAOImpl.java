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

import io.clownfish.clownfish.daointerface.CfSearchdatabaseDAO;
import io.clownfish.clownfish.dbentities.CfSearchdatabase;
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
public class CfSearchdatabaseDAOImpl implements CfSearchdatabaseDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSearchdatabaseDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfSearchdatabase findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchdatabase.findById");  
        query.setParameter("id", id);
        CfSearchdatabase cfsearchdatabase = (CfSearchdatabase) query.getSingleResult();
        return cfsearchdatabase;
    }

    @Override
    public CfSearchdatabase create(CfSearchdatabase entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfSearchdatabase entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfSearchdatabase edit(CfSearchdatabase entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfSearchdatabase> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchdatabase.findAll");  
        List<CfSearchdatabase> cfsearchdatabaselist = query.getResultList();
        return cfsearchdatabaselist;
    }

    @Override
    public CfSearchdatabase findByDatasourceRef(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchdatabase.findByDatasourceRef");  
        query.setParameter("id", id);
        CfSearchdatabase cfsearchdatabase = (CfSearchdatabase) query.getSingleResult();
        return cfsearchdatabase;
    }

    @Override
    public CfSearchdatabase findByDatasourceRefAndTable(Long id, String table) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSearchdatabase.findByDatasourceRefAndTable");  
        query.setParameter("datasourceRef", id);
        query.setParameter("tablename", table);
        CfSearchdatabase cfsearchdatabase = (CfSearchdatabase) query.getSingleResult();
        return cfsearchdatabase;
    }

}
