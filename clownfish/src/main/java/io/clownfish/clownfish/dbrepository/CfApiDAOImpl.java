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

import io.clownfish.clownfish.daointerface.CfApiDAO;
import io.clownfish.clownfish.dbentities.CfApi;
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
public class CfApiDAOImpl implements CfApiDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfApiDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    

    @Override
    public CfApi create(CfApi entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfApi entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfApi edit(CfApi entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfApi> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfApi.findAll");
        List<CfApi> cfapilist = query.getResultList();
        return cfapilist;
    }

    @Override
    public List<CfApi> findBySiteRef(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfApi.findBySiteref");
        query.setParameter("siteref", id);
        List<CfApi> cfapilist = query.getResultList();
        return cfapilist;
    }

    @Override
    public List<CfApi> findByKeyname(String keyname) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfApi.findByKeyname");
        query.setParameter("keyname", keyname);
        List<CfApi> cfapilist = query.getResultList();
        return cfapilist;
    }

    @Override
    public CfApi findBySiteRefAndKeyname(Long id, String keyname) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfApi.findBySiteRefAndKeyname");
        query.setParameter("siteref", id);
        query.setParameter("keyname", keyname);
        CfApi cfapi = (CfApi) query.getSingleResult();
        return cfapi;
    }
}
