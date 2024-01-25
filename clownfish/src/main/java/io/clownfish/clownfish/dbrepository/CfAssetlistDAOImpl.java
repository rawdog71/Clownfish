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

import io.clownfish.clownfish.daointerface.CfAssetlistDAO;
import io.clownfish.clownfish.dbentities.CfAssetlist;
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
public class CfAssetlistDAOImpl implements CfAssetlistDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfAssetlistDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfAssetlist findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlist.findById");  
        query.setParameter("id", id);
        CfAssetlist cfassetlist = (CfAssetlist) query.getSingleResult();
        return cfassetlist;
    }

    @Override
    public CfAssetlist create(CfAssetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAssetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAssetlist edit(CfAssetlist entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfAssetlist> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlist.findAll");
        List<CfAssetlist> cfassetlist = query.getResultList();
        return cfassetlist;
    }

    @Override
    public CfAssetlist findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlist.findByName");  
        query.setParameter("name", name);
        CfAssetlist cfassetlist = (CfAssetlist) query.getSingleResult();
        return cfassetlist;
    }

    @Override
    public List<CfAssetlist> findByNameLike(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlist.findByNameLike");  
        query.setParameter("name", name);
        List<CfAssetlist> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }
}
