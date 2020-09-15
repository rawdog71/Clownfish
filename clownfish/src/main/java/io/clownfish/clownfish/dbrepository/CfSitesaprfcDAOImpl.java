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

import io.clownfish.clownfish.daointerface.CfSitesaprfcDAO;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import java.util.List;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sulzbachr
 */
@Repository
public class CfSitesaprfcDAOImpl implements CfSitesaprfcDAO {
    
    private final SessionFactory sessionFactory;

    public CfSitesaprfcDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfSitesaprfc> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitesaprfc.findAll");
        List<CfSitesaprfc> cfsitesaprfclist = query.getResultList();
        return cfsitesaprfclist;
    }

    @Override
    public List<CfSitesaprfc> findBySiteref(long siteref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitesaprfc.findBySiteref");
        query.setParameter("siteref", siteref);
        List<CfSitesaprfc> cfsitesaprfclist = query.getResultList();
        return cfsitesaprfclist;
    }

    @Override
    public List<CfSitesaprfc> findByRfcgroup(String rfcgroup) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitesaprfc.findByRfcgroup");
        query.setParameter("rfcgroup", rfcgroup);
        List<CfSitesaprfc> cfsitesaprfclist = query.getResultList();
        return cfsitesaprfclist;
    }

    @Override
    public List<CfSitesaprfc> findByRfcfunction(String rfcfunction) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitesaprfc.findByRfcfunction");
        query.setParameter("rfcfunction", rfcfunction);
        List<CfSitesaprfc> cfsitesaprfclist = query.getResultList();
        return cfsitesaprfclist;
    }

    @Override
    public CfSitesaprfc create(CfSitesaprfc entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfSitesaprfc entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfSitesaprfc edit(CfSitesaprfc entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }
    
}
