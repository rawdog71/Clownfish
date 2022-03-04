package io.clownfish.clownfish.dbrepository;

import io.clownfish.clownfish.daointerface.CfContentversionDAO;
import io.clownfish.clownfish.dbentities.CfContentversion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class CfContentversionDAOImpl implements CfContentversionDAO
{
    private final SessionFactory sessionFactory;

    @Autowired
    public CfContentversionDAOImpl(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfContentversion> findByContentref(long ref)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfContentversion.findByContentref");
        query.setParameter("contentref", ref);
        List<CfContentversion> CfContentversionlist = query.getResultList();
        return CfContentversionlist;
    }

    @Override
    public long findMaxVersion(long ref)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfContentversion.findMaxVersion");
        query.setParameter("contentref", ref);
        long cfcontentversion = (long) query.getSingleResult();
        return cfcontentversion;
    }

    @Override
    public CfContentversion create(CfContentversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfContentversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfContentversion edit(CfContentversion entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public List<CfContentversion> findAll()
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfContentversion.findAll");
        List<CfContentversion> cfcontentversionlist = query.getResultList();
        return cfcontentversionlist;
    }

    @Override
    public CfContentversion findByPK(long ref, long version)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfContentversion.findByPK");
        query.setParameter("contentref", ref);
        query.setParameter("version", version);
        CfContentversion cfcontentversion = (CfContentversion) query.getSingleResult();
        return cfcontentversion;
    }
}
