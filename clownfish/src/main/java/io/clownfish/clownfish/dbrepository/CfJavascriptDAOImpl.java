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

import io.clownfish.clownfish.daointerface.CfJavascriptDAO;
import io.clownfish.clownfish.dbentities.CfJavascript;
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
public class CfJavascriptDAOImpl implements CfJavascriptDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public CfJavascriptDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public List<CfJavascript> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascript.findAll");
        List<CfJavascript> cfjavascriptlist = query.getResultList();
        return cfjavascriptlist;
    }

    @Override
    public CfJavascript create(CfJavascript entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfJavascript entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfJavascript edit(CfJavascript entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public CfJavascript findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascript.findById");
        query.setParameter("id", id);
        try {
            CfJavascript cfjavascript = (CfJavascript) query.getSingleResult();
            return cfjavascript;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfJavascript findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascript.findByName");
        query.setParameter("name", name);
        try {
            CfJavascript cfjavascript = (CfJavascript) query.getSingleResult();
            return cfjavascript;
        } catch (Exception ex) {
            return null;
        }
    }
}
