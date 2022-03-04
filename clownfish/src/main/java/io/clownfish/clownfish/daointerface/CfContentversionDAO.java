package io.clownfish.clownfish.daointerface;

import io.clownfish.clownfish.dbentities.CfContentversion;

import java.util.List;

public interface CfContentversionDAO
{
    List<CfContentversion> findByContentref(long ref);
    long findMaxVersion(long ref);
    CfContentversion findByPK(long ref, long version);
    List<CfContentversion> findAll();
    CfContentversion create(CfContentversion entity);
    boolean delete(CfContentversion entity);
    CfContentversion edit(CfContentversion entity);
}
