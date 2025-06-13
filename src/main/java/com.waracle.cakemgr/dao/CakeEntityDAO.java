package com.waracle.cakemgr.dao;

import com.waracle.cakemgr.entity.CakeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CakeEntityDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CakeEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CakeEntity c", CakeEntity.class)
                .getResultList();
    }

    public Optional<CakeEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(CakeEntity.class, id));
    }

    @Transactional
    public void persistRecord(CakeEntity cakeEntity) {
        entityManager.persist(cakeEntity);
    }

    @Transactional
    public void deleteRecord(CakeEntity cakeEntity) {
        entityManager.remove(cakeEntity);
    }

    @Transactional
    public void updateRecord(CakeEntity cakeEntity) {
        entityManager.merge(cakeEntity);
    }

}
