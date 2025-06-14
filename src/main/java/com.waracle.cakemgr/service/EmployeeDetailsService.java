package com.waracle.cakemgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waracle.cakemgr.entity.EmployeeEntity;
import com.waracle.cakemgr.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@Service
public class EmployeeDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeDetailsService.class);
    private final EmployeeRepository repo;

    public EmployeeDetailsService(EmployeeRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        LOG.info("Initializing EmployeeDetailsService...");
        if (repo.count() == 0) {
            LOG.info("No employees found in the database, importing from JSON...");
            importEmployeesFromJson();
        } else {
            LOG.info("Employees already exist in the database, skipping import.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EmployeeEntity emp = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(emp.getUsername())
                .password("{noop}password")  // for mock purposes, password is not important
                .roles(emp.getRole().name())
                .build();
    }

    private void importEmployeesFromJson() {

        List<EmployeeEntity> employees;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("employees.json")) {
            if (is == null) {
                throw new FileNotFoundException("employees.json not found in resources");
            } else {

                ObjectMapper objectMapper = new ObjectMapper();
                employees = objectMapper.readValue(
                        is.readAllBytes(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeEntity.class));

                for (EmployeeEntity e : employees) {
                    try {
                        repo.save(e);
                        LOG.info("Added employee: '{}'", e.getUsername());
                    } catch (DataIntegrityViolationException ex) {
                        LOG.error("Encountered data integrity violation for cake '{}': ", e.getEmployeeId(), ex);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("An unknown error occurred during load of Employees: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        LOG.info("Employee persistence completed successfully! Total employees from JSON: {}", employees.size());
    }

}

