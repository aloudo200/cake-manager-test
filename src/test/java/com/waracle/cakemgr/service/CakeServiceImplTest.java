package com.waracle.cakemgr.service;

import com.waracle.cakemgr.dto.CakeEntityDTO;
import com.waracle.cakemgr.entity.CakeEntity;
import com.waracle.cakemgr.exception.RecordAlreadyExistsException;
import com.waracle.cakemgr.exception.RecordNotFoundException;
import com.waracle.cakemgr.repository.CakeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CakeServiceImplTest {

    @Mock
    private CakeRepository cakeRepository;

    @InjectMocks
    private CakeServiceImpl cakeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyLoadFromRemote_savesUniqueCakesToRepo() {
        cakeService.loadFromRemote();
        verify(cakeRepository, times(5)).save(any(CakeEntity.class));
    }

    @Test
    void retrieveAllCakes_returnsList() {
        List<CakeEntity> cakes = List.of(new CakeEntity());
        when(cakeRepository.findAll()).thenReturn(cakes);

        List<CakeEntity> result = cakeService.retrieveAllCakes();

        assertEquals(1, result.size());
        verify(cakeRepository).findAll();
    }

    @Test
    void retrieveAllCakes_throwsRuntimeException_onError() {
        when(cakeRepository.findAll()).thenThrow(new RuntimeException("some unknown error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cakeService.retrieveAllCakes());
        assertTrue(ex.getMessage().contains("Failed to retrieve cakes"));
    }

    @Test
    void retrieveCakeById_returnsCake() {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));

        CakeEntity result = cakeService.retrieveCakeById(1);

        assertEquals(1, result.getCakeId());
    }

    @Test
    void retrieveCakeById_throwsNotFound() {
        when(cakeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> cakeService.retrieveCakeById(1));
    }

    @Test
    void addCake_savesCake() throws RecordAlreadyExistsException {
        CakeEntityDTO dto = new CakeEntityDTO();
        dto.setTitle("Test");
        dto.setDescription("desc");
        dto.setImageUrl("img");

        cakeService.addCake(dto);

        verify(cakeRepository).save(any(CakeEntity.class));
    }

    @Test
    void addCake_throwsAlreadyExists_onIntegrityViolation() {
        CakeEntityDTO dto = new CakeEntityDTO();
        dto.setTitle("Test");
        when(cakeRepository.save(any())).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(RecordAlreadyExistsException.class, () -> cakeService.addCake(dto));
    }

    @Test
    void addCake_throwsRuntimeException_onOtherError() {
        CakeEntityDTO dto = new CakeEntityDTO();
        dto.setTitle("Test");
        when(cakeRepository.save(any())).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> cakeService.addCake(dto));
    }

    @Test
    void deleteCake_deletesCake() {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));

        cakeService.deleteCake(1);

        verify(cakeRepository).delete(cake);
    }

    @Test
    void deleteCake_throwsNotFound() {
        when(cakeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> cakeService.deleteCake(1));
    }

    @Test
    void deleteCake_throwsRuntimeException_onError() {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));
        doThrow(new RuntimeException("fail")).when(cakeRepository).delete(any());

        assertThrows(RuntimeException.class, () -> cakeService.deleteCake(1));
    }

    @Test
    void updateCake_updatesFields() throws RecordAlreadyExistsException {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));
        when(cakeRepository.save(any())).thenReturn(cake);

        List<String> updated = cakeService.updateCake(1, "newTitle", "newDesc", "newImg");

        assertTrue(updated.containsAll(List.of("title", "description", "imageUrl")));
        verify(cakeRepository).save(cake);
    }

    @Test
    void updateCake_updatesPartialFields() throws RecordAlreadyExistsException {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));
        when(cakeRepository.save(any())).thenReturn(cake);

        List<String> updated = cakeService.updateCake(1, null, "desc", null);

        assertEquals(List.of("description"), updated);
    }

    @Test
    void updateCake_throwsAlreadyExists_onIntegrityViolation() {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));
        when(cakeRepository.save(any())).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(RecordAlreadyExistsException.class, () -> cakeService.updateCake(1, "t", null, null));
    }

    @Test
    void updateCake_throwsRuntimeException_onOtherError() {
        CakeEntity cake = new CakeEntity();
        cake.setCakeId(1);
        when(cakeRepository.findById(1)).thenReturn(Optional.of(cake));
        when(cakeRepository.save(any())).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> cakeService.updateCake(1, "t", null, null));
    }

    @Test
    void updateCake_throwsNotFound() {
        when(cakeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> cakeService.updateCake(1, "t", null, null));
    }
}