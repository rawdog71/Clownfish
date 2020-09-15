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

import io.clownfish.clownfish.daointerface.CfWebserviceDAO;
import io.clownfish.clownfish.dbentities.CfWebservice;
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
public class CfWebserviceDAOImpl implements CfWebserviceDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfWebserviceDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfWebservice findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfWebservice.findById");  
        query.setParameter("id", id);
        CfWebservice cfwebservice = (CfWebservice) query.getSingleResult();
        return cfwebservice;
    }

    @Override
    public CfWebservice findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfWebservice.findByName");  
        query.setParameter("name", name);
        CfWebservice cfwebservice = (CfWebservice) query.getSingleResult();
        return cfwebservice;
    }

    @Override
    public CfWebservice create(CfWebservice entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfWebservice entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfWebservice edit(CfWebservice entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfWebservice> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfWebservice.findAll");  
        List<CfWebservice> cfwebservicelist = query.getResultList();
        return cfwebservicelist;
    }
}
