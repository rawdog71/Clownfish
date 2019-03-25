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

import io.clownfish.clownfish.daointerface.CfListcontentDAO;
import io.clownfish.clownfish.dbentities.CfListcontent;
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
public class CfListcontentDAOImpl implements CfListcontentDAO {
    private final SessionFactory sessionFactory;
    
    @Autowired
    public CfListcontentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfListcontent> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfListcontent.findAll");
        List<CfListcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfListcontent> findByListref(long listref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfListcontent.findByListref");
        query.setParameter("listref", listref);
        List<CfListcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    @Override
    public List<CfListcontent> findByClasscontentref(long classcontentref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfListcontent.findByClasscontentref");
        query.setParameter("classcontentref", classcontentref);
        List<CfListcontent> cfcontentlist = query.getResultList();
        return cfcontentlist;
    }

    
}
