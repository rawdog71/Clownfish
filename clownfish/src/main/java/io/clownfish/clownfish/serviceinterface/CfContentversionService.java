package io.clownfish.clownfish.serviceinterface;

import io.clownfish.clownfish.dbentities.CfContentversion;

import java.util.List;

public interface CfContentversionService
{
    List<CfContentversion> findByContentref(long ref);
    long findMaxVersion(long ref);
    CfContentversion findByPK(long ref, long version);
    List<CfContentversion> findAll();
    CfContentversion create(CfContentversion entity);
    boolean delete(CfContentversion entity);
    CfContentversion edit(CfContentversion entity);
}
