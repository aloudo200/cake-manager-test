package com.waracle.cakemgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waracle.cakemgr.entity.EmployeeEntity;
import com.waracle.cakemgr.security.Role;
import com.waracle.cakemgr.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeDetailsServiceTest {

    @Mock
    private EmployeeRepository repo;

    @InjectMocks
    private EmployeeDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_returnsUserDetails() {
        EmployeeEntity emp = new EmployeeEntity();
        emp.setUsername("tester");
        emp.setRole(Role.USER);

        when(repo.findByUsername("tester")).thenReturn(Optional.of(emp));

        UserDetails user = service.loadUserByUsername("tester");

        assertEquals("tester", user.getUsername());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_throwsIfNotFound() {
        when(repo.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }

    @Test
    void init_importsEmployeesIfRepoEmpty() {
        EmployeeDetailsService spyService = Mockito.spy(new EmployeeDetailsService(repo));
        when(repo.count()).thenReturn(0L);
        doNothing().when(spyService).importEmployeesFromJson();

        spyService.init();

        verify(spyService).importEmployeesFromJson();
    }

    @Test
    void init_skipsImportIfRepoNotEmpty() {
        EmployeeDetailsService spyService = Mockito.spy(new EmployeeDetailsService(repo));
        when(repo.count()).thenReturn(2L);

        spyService.init();

        verify(spyService, never()).importEmployeesFromJson();
    }

    @Test
    void importEmployeesFromJson_importsAllEmployees() throws Exception {
        List<EmployeeEntity> employees = List.of(
                createEmployee(1, "tester", Role.USER),
                createEmployee(2, "headchef", Role.CHEF)
        );

        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper.writeValueAsBytes(employees);

        ClassLoader cl = mock(ClassLoader.class);
        InputStream is = new ByteArrayInputStream(json);
        when(cl.getResourceAsStream("employees.json")).thenReturn(is);

        EmployeeDetailsService testService = new EmployeeDetailsService(repo);

        testService.importEmployeesFromJson();

        verify(repo, times(2)).save(any(EmployeeEntity.class));
    }

    private EmployeeEntity createEmployee(int id, String username, Role role) {
        EmployeeEntity e = new EmployeeEntity();
        e.setEmployeeId(id);
        e.setUsername(username);
        e.setRole(role);
        return e;
    }
}