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

import io.clownfish.clownfish.daointerface.CfWebserviceauthDAO;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.dbentities.CfWebserviceauth;
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
public class CfWebserviceauthDAOImpl implements CfWebserviceauthDAO {

    private final SessionFactory sessionFactory;
    
    @Autowired 
    public CfWebserviceauthDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public CfWebserviceauth findByHash(String hash) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfWebserviceauth.findByHash");  
        query.setParameter("hash", hash);
        CfWebserviceauth cfwebserviceauth = (CfWebserviceauth) query.getSingleResult();
        return cfwebserviceauth;
    }

    @Override
    public CfWebserviceauth create(CfWebserviceauth entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfWebserviceauth entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfWebserviceauth edit(CfWebserviceauth entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfWebserviceauth> findByUserRef(CfUser ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfWebserviceauth.findByUserRef");  
        query.setParameter("userRef", ref);
        List<CfWebserviceauth> cfwebserviceauthlist = query.getResultList();
        return cfwebserviceauthlist;
    }
}
