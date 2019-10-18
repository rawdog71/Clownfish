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

import io.clownfish.clownfish.daointerface.CfSiteassetlistDAO;
import io.clownfish.clownfish.dbentities.CfSiteassetlist;
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
public class CfSiteassetlistDAOImpl implements CfSiteassetlistDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSiteassetlistDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfSiteassetlist> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSiteassetlist.findAll");
        List<CfSiteassetlist> cfsiteassetlist = query.getResultList();
        return cfsiteassetlist;
    }
    
    @Override
    public List<CfSiteassetlist> findBySiteref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSiteassetlist.findBySiteref");  
        query.setParameter("siteref", ref);
        List<CfSiteassetlist> cfsiteassetlist = query.getResultList();
        return cfsiteassetlist;
    }

    @Override
    public boolean create(CfSiteassetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfSiteassetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfSiteassetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfSiteassetlist> findByAssetlistref(Long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSiteassetlist.findByAssetlistref");
        query.setParameter("assetlistref", ref);
        List<CfSiteassetlist> cfsiteassetlist = query.getResultList();
        return cfsiteassetlist;
    }
}
