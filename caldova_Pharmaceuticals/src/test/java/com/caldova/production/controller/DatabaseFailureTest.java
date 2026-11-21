package com.caldova.production.controller;

import com.caldova.production.repository.ProductionRepository;
import com.caldova.production.testsupport.SecurityMockMvcSupportConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityMockMvcSupportConfiguration.class)
@WithMockUser("presenter")
class DatabaseFailureTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private ProductionRepository repository;

    @Test
    void oracleFailureReturns503ForBothHtmlAndHealthWithoutSubstituteRecords() throws Exception {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("private-db-context");
        when(repository.lines(anyString(), anyString())).thenThrow(failure);
        doThrow(failure).when(repository).checkConnection();
        mvc.perform(get("/lines"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("error"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private-db-context"))));
        mvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.services.oracle").value("DOWN"));
        mvc.perform(get("/lines").param("plant", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
