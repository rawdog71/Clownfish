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

import io.clownfish.clownfish.daointerface.CfSitedatasourceDAO;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
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
public class CfSitedatasourceDAOImpl implements CfSitedatasourceDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfSitedatasourceDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfSitedatasource> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitedatasource.findAll");
        List<CfSitedatasource> cfsitedatasourcelist = query.getResultList();
        return cfsitedatasourcelist;
    }
    
    @Override
    public List<CfSitedatasource> findBySiteref(Long siteref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitedatasource.findBySiteref");  
        query.setParameter("siteref", siteref);
        List<CfSitedatasource> cfsitedatasourcelist = query.getResultList();
        return cfsitedatasourcelist;
    }

    @Override
    public boolean create(CfSitedatasource entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return true;
    }

    @Override
    public boolean delete(CfSitedatasource entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public boolean edit(CfSitedatasource entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return true;
    }

    @Override
    public List<CfSitedatasource> findByDatasourceref(Long datasourceref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfSitedatasource.findByDatasourceref");
        query.setParameter("datasourceref", datasourceref);
        List<CfSitedatasource> cfsitedatasourcelist = query.getResultList();
        return cfsitedatasourcelist;
    }
}
