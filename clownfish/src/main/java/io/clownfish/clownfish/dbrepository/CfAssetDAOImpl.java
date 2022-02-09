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

import io.clownfish.clownfish.daointerface.CfAssetDAO;
import io.clownfish.clownfish.dbentities.CfAsset;
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
public class CfAssetDAOImpl implements CfAssetDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfAssetDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfAsset findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findById");  
        query.setParameter("id", id);
        CfAsset cfasset = (CfAsset) query.getSingleResult();
        if (!cfasset.isScrapped()) {
            return cfasset;
        } else {
            return null;
        }
    }

    @Override
    public CfAsset create(CfAsset entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAsset entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAsset edit(CfAsset entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfAsset> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findAll");
        List<CfAsset> cfassetlist = query.getResultList();
        return cfassetlist;
    }

    @Override
    public CfAsset findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findByName");  
        query.setParameter("name", name);
        CfAsset cfasset = (CfAsset) query.getSingleResult();
        return cfasset;
    }

    @Override
    public List<CfAsset> findByIndexed(boolean indexed) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findByIndexed");
        query.setParameter("indexed", indexed);
        List<CfAsset> cfassetlist = query.getResultList();
        return cfassetlist;
    }

    @Override
    public List<CfAsset> findByScrapped(boolean scrapped) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findByScrapped");
        query.setParameter("scrapped", scrapped);
        List<CfAsset> cfassetlist = query.getResultList();
        return cfassetlist;
    }

    @Override
    public List<CfAsset> findByPublicuse(boolean publicuse) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findByPublicuse");
        query.setParameter("publicuse", publicuse);
        List<CfAsset> cfassetlist = query.getResultList();
        return cfassetlist;
    }

}
