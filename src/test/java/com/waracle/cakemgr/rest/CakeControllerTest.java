package com.waracle.cakemgr.rest;

import com.waracle.cakemgr.config.SecurityConfig;
import com.waracle.cakemgr.config.TestConfig;
import com.waracle.cakemgr.entity.CakeEntity;
import com.waracle.cakemgr.exception.CustomAccessDeniedHandler;
import com.waracle.cakemgr.exception.GlobalExceptionHandler;
import com.waracle.cakemgr.exception.RecordNotFoundException;
import com.waracle.cakemgr.service.CakeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CakeController.class,
            excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class))
@Import({TestConfig.class, GlobalExceptionHandler.class})
class CakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CakeService cakeService;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    void getAllCakes_returnsOk() throws Exception {

        when(cakeService.retrieveAllCakes()).thenReturn(Collections.singletonList(
                new CakeEntity(1, "Lemon Cheesecake", "a cheesecake made of lemon", "imageUrl")));

        mockMvc.perform(get("/rest/cakes/getAllCakes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cakeId").value(1))
                .andExpect(jsonPath("$[0].title").value("Lemon Cheesecake"))
                .andExpect(jsonPath("$[0].desc").value("a cheesecake made of lemon"))
                .andExpect(jsonPath("$[0].image").value("imageUrl"));
    }

    @Test
    void getAllCakes_returnsInternalServerError() throws Exception {

        when(cakeService.retrieveAllCakes()).thenThrow(new RuntimeException());

        mockMvc.perform(get("/rest/cakes/getAllCakes"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCakeById_returnsOk() throws Exception {
        when(cakeService.retrieveCakeById(1)).thenReturn(new CakeEntity(1, "Lemon Cheesecake", "a cheesecake made of lemon", "imageUrl"));

        mockMvc.perform(get("/rest/cakes/getCakeById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cakeId").value(1))
                .andExpect(jsonPath("$.title").value("Lemon Cheesecake"))
                .andExpect(jsonPath("$.desc").value("a cheesecake made of lemon"))
                .andExpect(jsonPath("$.image").value("imageUrl"));
    }

    @Test
    void getCakeById_returnsNotFound_dueToRecordNotFoundException() throws Exception {
        when(cakeService.retrieveCakeById(anyInt())).thenThrow(new RecordNotFoundException("Cake with id '1' does not exist"));

        mockMvc.perform(get("/rest/cakes/getCakeById/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCake_returnsOk() throws Exception {
        when(cakeService.updateCake(1, "New Cake Title", null, null))
                .thenReturn(Collections.singletonList("title"));

        mockMvc.perform(put("/rest/cakes/updateCake/1")
                        .param("title", "New Cake Title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Successfully updated '[title]' on cake with id 1"));
    }

    @Test
    void updateCake_returnsBadRequest_whenNoFieldsProvided() throws Exception {
        mockMvc.perform(put("/rest/cakes/updateCake/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewCake_returnsCreated() throws Exception {
        String requestBody = "{\"cakeId\":1,\"title\":\"Lemon Cheesecake\",\"desc\":\"a cheesecake made of lemon\",\"image\":\"imageUrl\"}";

        mockMvc.perform(post("/rest/cakes/addNewCake")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("Cake with title 'Lemon Cheesecake' created successfully"));
    }

    @Test
    void deleteCake_returnsOk() throws Exception {
        mockMvc.perform(delete("/rest/cakes/deleteCake/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Cake with id '1' deleted successfully"));
    }
}