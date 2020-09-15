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

import io.clownfish.clownfish.daointerface.CfTemplateversionDAO;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
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
public class CfTemplateversionDAOImpl implements CfTemplateversionDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public CfTemplateversionDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfTemplateversion> findByTemplateref(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfTemplateversion.findByTemplateref");
        query.setParameter("templateref", ref);
        List<CfTemplateversion> cftemplateversionlist = query.getResultList();
        return cftemplateversionlist;
    }

    @Override
    public long findMaxVersion(long ref) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfTemplateversion.findMaxVersion");
        query.setParameter("templateref", ref);
        long cftemplateversion = (long) query.getSingleResult();
        return cftemplateversion;
    }

    @Override
    public CfTemplateversion create(CfTemplateversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfTemplateversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfTemplateversion edit(CfTemplateversion entity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfTemplateversion> findAll() {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfTemplateversion.findAll");
        List<CfTemplateversion> cftemplateversionlist = query.getResultList();
        return cftemplateversionlist;
    }

    @Override
    public CfTemplateversion findByPK(long ref, long version) {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfTemplateversion.findByPK");
        query.setParameter("templateref", ref);
        query.setParameter("version", version);
        CfTemplateversion cftemplateversion = (CfTemplateversion) query.getSingleResult();
        return cftemplateversion;
    }
}
