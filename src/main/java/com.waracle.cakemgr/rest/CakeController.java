package com.waracle.cakemgr.rest;

import com.waracle.cakemgr.dto.CakeEntityDTO;
import com.waracle.cakemgr.entity.CakeEntity;
import com.waracle.cakemgr.exception.RecordAlreadyExistsException;
import com.waracle.cakemgr.service.CakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("rest/cakes")
public class CakeController {

    private static final Logger LOG = LoggerFactory.getLogger(CakeController.class);

    private final CakeService cakeService;

    public CakeController(CakeService cakeService) {
        this.cakeService = cakeService;
    }

    @GetMapping("/getAllCakes")
    @Operation(summary = "Retrieves all cakes from the database")
    public ResponseEntity<List<CakeEntity>> getAllCakes() {

        try {
            return new ResponseEntity<>(cakeService.retrieveAllCakes(), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error retrieving cakes: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateCake/{id}") // use PUT for idempotency
    @Operation(summary = "Updates a cake in the database")
    @ApiResponse(responseCode = "200", description = "Cake updated successfully")
    public ResponseEntity<String> updateCake(
            @PathVariable Integer id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl) throws RecordAlreadyExistsException {
        if (Stream.of(title, description, imageUrl).allMatch(Objects::isNull)) {
            LOG.error("No fields provided for update on cake with id {}", id);
            return new ResponseEntity<>(String.format("No fields provided for update on cake with id '%d'", id), HttpStatus.BAD_REQUEST);
        }
        List<String> updatedFields = cakeService.updateCake(id, title, description, imageUrl);
        return new ResponseEntity<>(String.format("Successfully updated '%s' on cake with id %d", updatedFields, id), HttpStatus.OK);
    }

    @PostMapping("/addNewCake")
    @Operation(summary = "Adds a new cake to the database")
    @ApiResponse(responseCode = "201", description = "Cake created successfully")
    public ResponseEntity<String> addNewCake(@RequestBody CakeEntityDTO cakeEntity) throws RecordAlreadyExistsException {
        cakeService.addCake(cakeEntity);
        return new ResponseEntity<>(String.format("Cake with title '%s' created successfully", cakeEntity.getTitle()), HttpStatus.CREATED);
    }

    @DeleteMapping("/deleteCake/{id}")
    @Operation(summary = "Removes a cake from the database")
    @ApiResponse(responseCode = "200", description = "Cake deleted successfully")
    public ResponseEntity<String> deleteCake(@PathVariable Integer id) {
        cakeService.deleteCake(id);
        return new ResponseEntity<>(String.format("Cake with id '%d' deleted successfully", id), HttpStatus.OK);
    }
}
