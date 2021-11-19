package io.clownfish.clownfish.dbrepository;

import io.clownfish.clownfish.daointerface.CfJavaversionDAO;
import io.clownfish.clownfish.dbentities.CfJavaversion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class CfJavaversionDAOImpl implements CfJavaversionDAO
{
    private final SessionFactory sessionFactory;

    @Autowired
    public CfJavaversionDAOImpl(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfJavaversion> findByJavaref(long ref)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavaversion.findByJavaref");
        query.setParameter("javaref", ref);
        List<CfJavaversion> CfJavaversionlist = query.getResultList();
        return CfJavaversionlist;
    }

    @Override
    public long findMaxVersion(long ref)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavaversion.findMaxVersion");
        query.setParameter("javaref", ref);
        long CfJavaversion = (long) query.getSingleResult();
        return CfJavaversion;
    }

    @Override
    public CfJavaversion create(CfJavaversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfJavaversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfJavaversion edit(CfJavaversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfJavaversion> findAll()
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavaversion.findAll");
        List<CfJavaversion> CfJavaversionlist = query.getResultList();
        return CfJavaversionlist;
    }

    @Override
    public CfJavaversion findByPK(long ref, long version)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJavaversion.findByPK");
        query.setParameter("javaref", ref);
        query.setParameter("version", version);
        CfJavaversion CfJavaversion = (CfJavaversion) query.getSingleResult();
        return CfJavaversion;
    }
}
