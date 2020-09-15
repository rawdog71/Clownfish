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

import io.clownfish.clownfish.daointerface.CfAssetlistcontentDAO;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
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
public class CfAssetlistcontentDAOImpl implements CfAssetlistcontentDAO {
    private final SessionFactory sessionFactory;
    
    @Autowired
    public CfAssetlistcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfAssetlistcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlistcontent.findAll");
        List<CfAssetlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfAssetlistcontent> findByAssetlistref(long assetlistref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlistcontent.findByAssetlistref");
        query.setParameter("assetlistref", assetlistref);
        List<CfAssetlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfAssetlistcontent> findByAssetref(long keywordref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAssetlistcontent.findByAssetref");
        query.setParameter("assetref", keywordref);
        List<CfAssetlistcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }
    
    @Override
    public CfAssetlistcontent create(CfAssetlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAssetlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAssetlistcontent edit(CfAssetlistcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }
}
