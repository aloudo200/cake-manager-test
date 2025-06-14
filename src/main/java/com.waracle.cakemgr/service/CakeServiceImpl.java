package com.waracle.cakemgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waracle.cakemgr.repository.CakeRepository;
import com.waracle.cakemgr.dto.CakeEntityDTO;
import com.waracle.cakemgr.entity.CakeEntity;
import com.waracle.cakemgr.exception.RecordAlreadyExistsException;
import com.waracle.cakemgr.exception.RecordNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.http.HTTPException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class CakeServiceImpl implements CakeService {

    private static final Logger LOG = LoggerFactory.getLogger(CakeServiceImpl.class);

    private final CakeRepository cakeRepository;

    public CakeServiceImpl(CakeRepository cakeRepository) {
        this.cakeRepository = cakeRepository;
    }

    @PostConstruct
    private void loadCakes() throws HTTPException {
        LOG.info("Starting automatic load of cakes into in-memory database...");
        loadFromRemote();
    }

    @Override
    public List<CakeEntity> retrieveAllCakes() {

        try {
            return cakeRepository.findAll();
        } catch (Exception e) {
            LOG.error("Error retrieving cakes from in-memory database: {} ", e.getMessage());
            throw new RuntimeException("Failed to retrieve cakes from database", e);
        }
    }

    @Override
    public CakeEntity retrieveCakeById(Integer id) {
        return cakeRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(String.format("Cake with id '%d' not found", id)));
    }

    @Override
    public void addCake(CakeEntityDTO newCake) throws RecordAlreadyExistsException {
        CakeEntity entity = new CakeEntity();

        entity.setTitle(newCake.getTitle());
        entity.setDesc(newCake.getDescription());
        entity.setImage(newCake.getImageUrl());

        try {
            cakeRepository.save(entity);
            LOG.info("Added new cake entity: '{}'", newCake.getTitle());
        } catch (DataIntegrityViolationException ex) {
            LOG.error("Data integrity violation for cake '{}': ", newCake.getTitle(), ex);
            throw new RecordAlreadyExistsException(String.format("Cake with title '%s' already exists", newCake.getTitle()));
        } catch (Exception e) {
            LOG.error("Error adding cake to in-memory database: {} ", e.getMessage());
            throw new RuntimeException("Failed to add cake to database", e);
        }
    }

    @Override
    public void deleteCake(Integer id) throws RecordNotFoundException {
        CakeEntity cake = retrieveCakeById(id);
        try {
            cakeRepository.delete(cake);
            LOG.info("Deleted cake entity: '{}'", cake.getTitle());
        } catch (Exception e) {
            LOG.error("Error deleting cake from in-memory database: {}", e.getMessage());
            throw new RuntimeException("Failed to delete cake from database", e);
        }
    }
    @Override
    public List<String> updateCake(Integer id, String title, String description, String imageUrl)
            throws RecordAlreadyExistsException, RecordNotFoundException {

        CakeEntity existingCake = retrieveCakeById(id);

        List<String> updatedFields = new ArrayList<>();

        Optional.ofNullable(title).ifPresent(t -> {
            existingCake.setTitle(t);
            updatedFields.add("title");
        });
        Optional.ofNullable(description).ifPresent(d -> {
            existingCake.setDesc(d);
            updatedFields.add("description");
        });
        Optional.ofNullable(imageUrl).ifPresent(i -> {
            existingCake.setImage(i);
            updatedFields.add("imageUrl");
        });

        try {
            cakeRepository.save(existingCake);
            LOG.info("Updated '{}' on cake entity with ID: '{}'", updatedFields, existingCake.getCakeId());
        } catch (DataIntegrityViolationException  ex) {
            LOG.error("Constraint violation updating cake '{}': ", existingCake.getTitle(), ex);
            throw new RecordAlreadyExistsException(
                    String.format("Cake with title '%s' already exists", existingCake.getTitle()));
        } catch (Exception e) {
            LOG.error("Error updating cake in in-memory database: {} ", e.getMessage());
            throw new RuntimeException("Failed to update cake in database", e);
        }
        return updatedFields;
    }

    private void loadFromRemote() throws HTTPException {

        HashSet<CakeEntity> cakes = new HashSet<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            LOG.info("Downloading cake JSON from remote URL...");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json"))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            ObjectMapper objectMapper = new ObjectMapper();
            cakes = objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(Set.class, CakeEntity.class));

            for (CakeEntity cakeEntity : cakes) {
                try {
                    cakeRepository.save(cakeEntity);
                    LOG.info("Added cake entity: '{}'", cakeEntity.getTitle());
                } catch (DataIntegrityViolationException ex) {
                    LOG.error("Encountered data integrity violation for cake '{}': ", cakeEntity.getTitle(), ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("Error during cake JSON download and persistence: {} ", ex.getMessage());
        }
        LOG.info("Cake JSON download and persistence completed successfully! Total unique cakes from JSON: {}", cakes.size());
    }
}
