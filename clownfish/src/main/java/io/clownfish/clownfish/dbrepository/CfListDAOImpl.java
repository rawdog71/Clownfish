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

import io.clownfish.clownfish.daointerface.CfListDAO;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfList;
import java.math.BigInteger;
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
public class CfListDAOImpl implements CfListDAO {
    private final SessionFactory sessionFactory;
    
    @Autowired
    public CfListDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfList> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findAll");
        List<CfList> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public CfList findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findById");  
        query.setParameter("id", id);
        CfList cflist = (CfList) query.getSingleResult();
        return cflist;
    }

    @Override
    public CfList create(CfList entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfList entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfList edit(CfList entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public CfList findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByName");  
        query.setParameter("name", name);
        CfList cflist = (CfList) query.getSingleResult();
        return cflist;
    }

    @Override
    public List<CfList> findByClassref(CfClass ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByClassref");  
        query.setParameter("classref", ref);
        List<CfList> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfList> findByMaintenance(boolean b) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByMaintenance");  
        query.setParameter("maintenance", b);
        List<CfList> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public CfList findByClassrefAndName(CfClass ref, String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByClassrefAndName");  
        query.setParameter("classref", ref);
        query.setParameter("name", name);
        CfList cflist = (CfList) query.getSingleResult();
        return cflist;
    }

    @Override
    public List<CfList> findNotInList(BigInteger ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findNotInList");  
        query.setParameter("refclasscontent", ref);
        List<CfList> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public CfList findByNameNotInList(String name, BigInteger ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByNameNotInList");  
        query.setParameter("name", name);
        query.setParameter("refclasscontent", ref);
        CfList cflist = (CfList) query.getSingleResult();
        return cflist;
    }

    @Override
    public List<CfList> findByNameLike(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfList.findByNameLike");  
        query.setParameter("name", name);
        List<CfList> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }
}
