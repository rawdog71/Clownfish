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

import io.clownfish.clownfish.daointerface.CfQuartzDAO;
import io.clownfish.clownfish.dbentities.CfQuartz;
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
public class CfQuartzDAOImpl implements CfQuartzDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfQuartzDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfQuartz findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfQuartz.findById");  
        query.setParameter("id", id);
        try {
            CfQuartz cfquartz = (CfQuartz) query.getSingleResult();
            return cfquartz;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfQuartz findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfQuartz.findByName");  
        query.setParameter("name", name);
        try {
            CfQuartz cfquartz = (CfQuartz) query.getSingleResult();
            return cfquartz;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfQuartz create(CfQuartz entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfQuartz entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfQuartz edit(CfQuartz entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfQuartz> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfQuartz.findAll");
        List<CfQuartz> cfquartzlist = query.getResultList();
        return cfquartzlist;
    }
    
}
