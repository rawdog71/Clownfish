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

import io.clownfish.clownfish.daointerface.CfStylesheetversionDAO;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
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
public class CfStylesheetversionDAOImpl implements CfStylesheetversionDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public CfStylesheetversionDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfStylesheetversion> findByStylesheetref(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStylesheetversion.findByStylesheetref");
        query.setParameter("stylesheetref", ref);
        List<CfStylesheetversion> cfstylesheetversionlist = query.getResultList();
        return cfstylesheetversionlist;
    }

    @Override
    public long findMaxVersion(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStylesheetversion.findMaxVersion");
        query.setParameter("stylesheetref", ref);
        long cfstylesheetversion = (long) query.getSingleResult();
        return cfstylesheetversion;
    }

    @Override
    public boolean create(CfStylesheetversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfStylesheetversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfStylesheetversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfStylesheetversion> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStylesheetversion.findAll");
        List<CfStylesheetversion> cfstylesheetversionlist = query.getResultList();
        return cfstylesheetversionlist;
    }

    @Override
    public CfStylesheetversion findByPK(long ref, long version) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfStylesheetversion.findByPK");
        query.setParameter("stylesheetref", ref);
        query.setParameter("version", version);
        CfStylesheetversion cfstylesheetversion = (CfStylesheetversion) query.getSingleResult();
        return cfstylesheetversion;
    }
}
