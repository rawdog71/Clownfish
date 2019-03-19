/*
 * Copyright 2019 rawdog.
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

import io.clownfish.clownfish.daointerface.CfPropertyDAO;
import io.clownfish.clownfish.dbentities.CfProperty;
import java.util.List;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author rawdog
 */
public class CfPropertyDAOImpl implements CfPropertyDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public CfPropertyDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfProperty findByHashkey(String hashkey) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfProperty.findByHashkey");
        query.setParameter("hashkey", hashkey);
        CfProperty cfproperty = (CfProperty) query.getSingleResult();
        return cfproperty;
    }

    @Override
    public CfProperty findByValue(String value) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfProperty.findByValue");
        query.setParameter("value", value);
        CfProperty cfproperty = (CfProperty) query.getSingleResult();
        return cfproperty;
    }

    @Override
    public List<CfProperty> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfProperty.findAll");
        List<CfProperty> cfpropertylist = query.getResultList();
        return cfpropertylist;
    }

    @Override
    public boolean create(CfProperty entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfProperty entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfProperty entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }
    
}
