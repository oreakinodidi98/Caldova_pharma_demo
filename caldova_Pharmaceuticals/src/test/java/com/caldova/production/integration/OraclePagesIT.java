package com.caldova.production.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser("presenter")
class OraclePagesIT {
    @Autowired private MockMvc mvc;

    @Test
    void overviewFiltersAndLineNavigationRenderRealOracleRecords() throws Exception {
        MvcResult overview = mvc.perform(get("/lines")).andExpect(status().isOk())
                .andExpect(view().name("lines")).andExpect(model().attribute("totalLines", 160))
                .andExpect(model().attribute("lines", hasSize(160))).andReturn();
        Document document = Jsoup.parse(overview.getResponse().getContentAsString());
        assertEquals(160, document.select("tbody tr").size());
        assertEquals(1, document.select("h1").size());
        assertFalse(document.select("link[href='/css/legacy.css']").isEmpty());
        MvcResult filtered = mvc.perform(get("/lines").param("plant", "NB").param("status", "STOPPED"))
                .andExpect(status().isOk()).andExpect(model().attribute("lines", hasSize(3))).andReturn();
        Document table = Jsoup.parse(filtered.getResponse().getContentAsString());
        String href = table.select("a[href*='/lines/1?']").first().attr("href");
        assertTrue(href.contains("plant=NB") && href.contains("status=STOPPED"));
        mvc.perform(get(href)).andExpect(status().isOk()).andExpect(view().name("line-detail"))
                .andExpect(content().string(containsString("ALM-204")))
                .andExpect(content().string(containsString("Carton-present sensor")))
                .andExpect(content().string(containsString("Material availability")))
                .andExpect(content().string(containsString("CP-12 / 4.2")))
                .andExpect(content().string(containsString("2026-11-19")));
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void invalidInputsMissingLinesAndMutationMethodsAreRejected() throws Exception {
        for (String id : new String[]{"0", "-1", "abc", "99999999999999999999999"}) {
            mvc.perform(get("/lines/" + id)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/lines/9999")).andExpect(status().isNotFound());
        mvc.perform(get("/lines").param("plant", "not-a-plant")).andExpect(status().isBadRequest());
        mvc.perform(get("/lines/1").param("status", "unknown")).andExpect(status().isBadRequest());
        mvc.perform(post("/lines/1").with(csrf())).andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET, HEAD"));
    }

    @Test
    void aValidEmptyFilterResultDoesNotMasqueradeAsDatabaseFailure() throws Exception {
        try (Connection owner = owner()) {
            owner.setAutoCommit(false);
            try {
                owner.createStatement().executeUpdate(
                        "UPDATE CALDOVA_OWNER.production_line SET line_status='RUNNING' "
                        + "WHERE plant_id=2 AND line_status='ATTENTION'");
                owner.commit();
                mvc.perform(get("/lines").param("plant", "WH").param("status", "ATTENTION"))
                        .andExpect(status().isOk()).andExpect(model().attribute("lines", hasSize(0)));
            } finally {
                owner.createStatement().executeUpdate(
                        "UPDATE CALDOVA_OWNER.production_line SET line_status='ATTENTION' "
                        + "WHERE line_id IN (41,72,73,74,75,76)");
                owner.commit();
            }
        }
        mvc.perform(get("/lines").param("plant", "WH").param("status", "ATTENTION"))
                .andExpect(status().isOk()).andExpect(model().attribute("lines", hasSize(6)));
    }

    @Test
    void storedManualTextIsEscapedAndNotInterpretedAsMarkup() throws Exception {
        try (Connection owner = owner()) {
            String original;
            try (ResultSet rs = owner.createStatement().executeQuery(
                    "SELECT excerpt FROM CALDOVA_OWNER.manual_excerpt WHERE excerpt_id=11")) {
                assertTrue(rs.next()); original = rs.getString(1);
            }
            try {
                updateExcerpt(owner, "<script>alert('demo')</script>");
                MvcResult result = mvc.perform(get("/lines/1")).andExpect(status().isOk()).andReturn();
                Document page = Jsoup.parse(result.getResponse().getContentAsString());
                assertTrue(page.select("script").isEmpty());
                assertTrue(page.text().contains("<script>alert('demo')</script>"));
            } finally {
                updateExcerpt(owner, original);
            }
        }
    }

    private static void updateExcerpt(Connection connection, String value) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE CALDOVA_OWNER.manual_excerpt SET excerpt=? WHERE excerpt_id=11")) {
            statement.setString(1, value); statement.executeUpdate();
        }
    }

    static Connection owner() throws Exception {
        String password = System.getenv("ORACLE_SETUP_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Real Oracle setup credentials are required for verify.");
        }
        return DriverManager.getConnection(System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("ORACLE_SETUP_USER"), password);
    }
}
