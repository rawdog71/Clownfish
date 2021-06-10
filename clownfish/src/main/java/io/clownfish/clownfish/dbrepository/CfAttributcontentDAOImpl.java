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

import io.clownfish.clownfish.daointerface.CfAttributcontentDAO;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import java.util.List;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sulzbachr
 */
@Repository
public class CfAttributcontentDAOImpl implements CfAttributcontentDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfAttributcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfAttributcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findAll");
        List<CfAttributcontent> cfattributcontentlist = query.getResultList();
        return cfattributcontentlist;
    }
    
    @Override
    public CfAttributcontent create(CfAttributcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAttributcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAttributcontent edit(CfAttributcontent entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfAttributcontent> findByClasscontentref(CfClasscontent classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByClasscontentref");
        query.setParameter("classcontentref", classcontentref);
        List<CfAttributcontent> cfattributcontentlist = query.getResultList();
        return cfattributcontentlist;
    }

    @Override
    public CfAttributcontent findByAttributrefAndClasscontentref(CfAttribut attributref, CfClasscontent classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByAttributrefAndClasscontentref");
        query.setParameter("attributref", attributref);
        query.setParameter("classcontentref", classcontentref);
        CfAttributcontent cfattributcontent = (CfAttributcontent) query.getSingleResult();
        return cfattributcontent;
    }

    @Override
    public List<CfAttributcontent> findByIndexed(boolean indexed) {
        Session session = this.sessionFactory.getCurrentSession();
        //TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByIndexed");
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByIndexedAndSearchrelevant");
        query.setParameter("indexed", indexed);
        List<CfAttributcontent> cfattributcontentlist = query.getResultList();
        return cfattributcontentlist;
    }

    @Override
    public List<CfAttributcontent> findByContentclassRef(CfList classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByContentclassRef");
        query.setParameter("classcontentlistref", classcontentref);
        List<CfAttributcontent> cfattributcontentlist = query.getResultList();
        return cfattributcontentlist;
    }

    @Override
    public List<CfAttributcontent> findByContentAssetRef(CfAssetlist classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAttributcontent.findByContentAssetRef");
        query.setParameter("assetcontentlistref", classcontentref);
        List<CfAttributcontent> cfattributcontentlist = query.getResultList();
        return cfattributcontentlist;
    }

    @Override
    public boolean delete(long classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        NativeQuery query = (NativeQuery) session.createSQLQuery("DELETE FROM cf_attributcontent WHERE classcontentref = " + classcontentref);
        query.executeUpdate();
        return true;
    }

    @Override
    public boolean updateContentref(long contentref) {
        Session session = this.sessionFactory.getCurrentSession();
        NativeQuery query = (NativeQuery) session.createSQLQuery("UPDATE cf_attributcontent SET content_classref = null WHERE content_classref = " + contentref);
        query.executeUpdate();
        return true;
    }
}
