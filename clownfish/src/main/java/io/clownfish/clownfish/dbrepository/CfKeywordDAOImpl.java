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

import io.clownfish.clownfish.daointerface.CfKeywordDAO;
import io.clownfish.clownfish.dbentities.CfKeyword;
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
public class CfKeywordDAOImpl implements CfKeywordDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfKeywordDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfKeyword findById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeyword.findById");  
        query.setParameter("id", id);
        try {
            CfKeyword cfkeyword = (CfKeyword) query.getSingleResult();
            return cfkeyword;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfKeyword create(CfKeyword entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfKeyword entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfKeyword edit(CfKeyword entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfKeyword> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeyword.findAll");
        List<CfKeyword> cfkeywordlist = query.getResultList();
        return cfkeywordlist;
    }

    @Override
    public CfKeyword findByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeyword.findByName");  
        query.setParameter("name", name);
        try {
            CfKeyword cfkeyword = (CfKeyword) query.getSingleResult();
            return cfkeyword;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public List<CfKeyword> findByNameBeginning(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfKeyword.findByNameBeginning");
        query.setParameter("name", name + "%");
        List<CfKeyword> cfkeywordlist = query.getResultList();
        return cfkeywordlist;
    }
}
