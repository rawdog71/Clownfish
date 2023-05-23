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

import io.clownfish.clownfish.daointerface.CfAccessmanagerDAO;
import io.clownfish.clownfish.dbentities.CfAccessmanager;
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
public class CfAccessmanagerDAOImpl implements CfAccessmanagerDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfAccessmanagerDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfAccessmanager findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAsset.findById");  
        query.setParameter("id", id);
        try {
            CfAccessmanager cfam = (CfAccessmanager) query.getSingleResult();
            return cfam;
        } catch (Exception ex) {
            System.out.println("Accessmanager-ID not found: " + id);
            return null;
        }
    }

    @Override
    public CfAccessmanager create(CfAccessmanager entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfAccessmanager entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfAccessmanager edit(CfAccessmanager entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfAccessmanager> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findAll");
        List<CfAccessmanager> cfamlist = query.getResultList();
        return cfamlist;
    }

    @Override
    public List<CfAccessmanager> findByType(Integer type) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findByType");
        query.setParameter("type", type);
        List<CfAccessmanager> cfamlist = query.getResultList();
        return cfamlist;
    }

    @Override
    public List<CfAccessmanager> findByRef(BigInteger ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findByRef");
        query.setParameter("ref", ref);
        List<CfAccessmanager> cfamlist = query.getResultList();
        return cfamlist;
    }

    @Override
    public List<CfAccessmanager> findByRefclasscontent(BigInteger refclasscontent) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findByRefclasscontent");
        query.setParameter("refclasscontent", refclasscontent);
        List<CfAccessmanager> cfamlist = query.getResultList();
        return cfamlist;
    }

    @Override
    public CfAccessmanager findByTypeAndRefAndRefclasscontent(Integer type, BigInteger ref, BigInteger refclasscontent) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findByTypeAndRefAndRefclasscontent");
        query.setParameter("type", type);
        query.setParameter("ref", ref);
        query.setParameter("refclasscontent", refclasscontent);
        CfAccessmanager cfam = (CfAccessmanager) query.getSingleResult();
        return cfam;
    }

    @Override
    public List<CfAccessmanager> findByTypeAndRef(Integer type, BigInteger ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfAccessmanager.findByTypeAndRef");
        query.setParameter("type", type);
        query.setParameter("ref", ref);
        List<CfAccessmanager> cfamlist = query.getResultList();
        return cfamlist;
    }
}
