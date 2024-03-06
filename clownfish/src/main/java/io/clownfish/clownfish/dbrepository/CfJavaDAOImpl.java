package io.clownfish.clownfish.dbrepository;

import io.clownfish.clownfish.daointerface.CfJavaDAO;
import io.clownfish.clownfish.dbentities.CfJava;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class CfJavaDAOImpl implements CfJavaDAO
{

    private final SessionFactory sessionFactory;

    @Autowired
    public CfJavaDAOImpl(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<CfJava> findAll()
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJava.findAll");
        List<CfJava> CfJavalist = query.getResultList();
        return CfJavalist;
    }

    @Override
    public CfJava create(CfJava entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public boolean delete(CfJava entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.delete(entity);
        return true;
    }

    @Override
    public CfJava edit(CfJava entity)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.merge(entity);
        return entity;
    }

    @Override
    public CfJava findById(Long id)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJava.findById");
        query.setParameter("id", id);
        try {
            CfJava CfJava = (CfJava) query.getSingleResult();
            return CfJava;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CfJava findByName(String name)
    {
        Session session = this.sessionFactory.getCurrentSession();
        TypedQuery query = (TypedQuery) session.getNamedQuery("CfJava.findByName");
        query.setParameter("name", name);
        try {
            CfJava CfJava = (CfJava) query.getSingleResult();
            return CfJava;
        } catch (Exception ex) {
            return null;
        }
    }
}
