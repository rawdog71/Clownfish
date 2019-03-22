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

import io.clownfish.clownfish.daointerface.CfJavascriptversionDAO;
import io.clownfish.clownfish.dbentities.CfJavascriptversion;
import java.util.List;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author rawdog
 */
@Repository
public class CfJavascriptversionDAOImpl implements CfJavascriptversionDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public CfJavascriptversionDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfJavascriptversion> findByJavascriptref(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascriptversion.findByJavascriptref");
        query.setParameter("javascriptref", ref);
        List<CfJavascriptversion> cfjavascriptversionlist = query.getResultList();
        return cfjavascriptversionlist;
    }

    @Override
    public long findMaxVersion(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascriptversion.findMaxVersion");
        query.setParameter("javascriptref", ref);
        long cfjavascriptversion = (long) query.getSingleResult();
        return cfjavascriptversion;
    }

    @Override
    public boolean create(CfJavascriptversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfJavascriptversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfJavascriptversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfJavascriptversion> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascriptversion.findAll");
        List<CfJavascriptversion> cfjavascriptversionlist = query.getResultList();
        return cfjavascriptversionlist;
    }

    @Override
    public CfJavascriptversion findByPK(long ref, long version) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavascriptversion.findByPK");
        query.setParameter("javascriptref", ref);
        query.setParameter("version", version);
        CfJavascriptversion cfjavascriptversion = (CfJavascriptversion) query.getSingleResult();
        return cfjavascriptversion;
    }
}
