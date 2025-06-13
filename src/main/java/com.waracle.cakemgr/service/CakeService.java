package com.waracle.cakemgr.service;

import com.waracle.cakemgr.dto.CakeEntityDTO;
import com.waracle.cakemgr.entity.CakeEntity;
import com.waracle.cakemgr.exception.RecordAlreadyExistsException;
import com.waracle.cakemgr.exception.RecordNotFoundException;

import java.util.List;

public interface CakeService {

    List<CakeEntity> retrieveAllCakes();

    CakeEntity retrieveCakeById(Integer id) throws RecordNotFoundException;

    void addCake(CakeEntityDTO cakeEntity) throws RecordAlreadyExistsException;

    void updateCake(Integer id, String title, String description, String imageUrl) throws RecordAlreadyExistsException;

    void deleteCake(Integer id);

}
