package com.caldova.production.integration;

import com.caldova.production.model.OperationsRecords;
import com.caldova.production.repository.OperationsRepository;
import com.caldova.production.testsupport.SecurityMockMvcSupportConfiguration;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Import(SecurityMockMvcSupportConfiguration.class)
class OraclePortalIT {
    @Autowired private MockMvc mvc;
    @Autowired private OperationsRepository operations;
    @Autowired private DataSource datasource;
    @Autowired private PasswordEncoder encoder;

    @Test
    void oracleSignInProtectsPagesAndLogoutEndsTheSession() throws Exception {
        for (String path : new String[]{"/","/lines","/maintenance","/rota","/lines/1"}) {
            mvc.perform(get(path)).andExpect(status().isFound()).andExpect(redirectedUrlPattern("**/login"));
        }
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        MvcResult result = mvc.perform(post("/login").with(csrf()).param("username","presenter")
                        .param("password",password()))
                .andExpect(status().isFound()).andExpect(authenticated().withUsername("presenter")).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andExpect(view().name("overview")).andExpect(model().attribute("totalLines",160))
                .andExpect(model().attribute("plantSummaries",hasSize(4)))
                .andExpect(model().attribute("upcomingMaintenance",hasSize(8)))
                .andExpect(model().attribute("shiftSummaries",hasSize(8)));
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?logout"));
        assertTrue(session.isInvalid());
        mvc.perform(get("/rota")).andExpect(status().isFound());
    }

    @Test
    void badOrDisabledCredentialsAndMissingCsrfAreRejectedWithoutChangingTheAccount() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username","presenter").param("password","incorrect"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        mvc.perform(post("/login").param("username","presenter").param("password",password()))
                .andExpect(status().isForbidden()).andExpect(unauthenticated());
        try (Connection owner = OraclePagesIT.owner(); Statement statement = owner.createStatement()) {
            int enabled;
            try (ResultSet rs = statement.executeQuery("SELECT enabled FROM CALDOVA_OWNER.app_user WHERE username='presenter'")) {
                assertTrue(rs.next()); enabled = rs.getInt(1);
            }
            try {
                statement.executeUpdate("UPDATE CALDOVA_OWNER.app_user SET enabled=0 WHERE username='presenter'");
                mvc.perform(post("/login").with(csrf()).param("username","presenter").param("password",password()))
                        .andExpect(status().isFound()).andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
            } finally {
                statement.executeUpdate("UPDATE CALDOVA_OWNER.app_user SET enabled="+enabled+" WHERE username='presenter'");
            }
        }
    }

    @Test
    void operationsPagesFilterRealRecordsAndHandleEmptyOrInvalidRotaDates() throws Exception {
        mvc.perform(get("/maintenance").with(user("presenter")))
                .andExpect(status().isOk()).andExpect(model().attribute("work",hasSize(12)));
        mvc.perform(get("/maintenance").with(user("presenter")).param("plant","NB").param("status","COMPLETED"))
                .andExpect(status().isOk()).andExpect(model().attribute("work",hasSize(1)))
                .andExpect(content().string(containsString("Recorded equipment inspection")));
        mvc.perform(get("/maintenance").with(user("presenter")).param("plant","NB").param("line","2").param("status","IN_PROGRESS"))
                .andExpect(status().isOk()).andExpect(model().attribute("work",hasSize(1)))
                .andExpect(content().string(containsString("Planned service review")));
        mvc.perform(get("/maintenance").with(user("presenter")).param("plant","WH").param("line","2"))
                .andExpect(status().isOk()).andExpect(model().attribute("work",hasSize(0)));
        mvc.perform(get("/maintenance").with(user("presenter")).param("line","0")).andExpect(status().isBadRequest());
        mvc.perform(get("/lines/1").with(user("presenter"))).andExpect(status().isOk())
                .andExpect(model().attribute("maintenance",hasSize(1)))
                .andExpect(content().string(containsString("Recorded equipment inspection")));
        mvc.perform(get("/rota").with(user("presenter")).param("plant","NB").param("date","2026-11-20").param("shift","NIGHT"))
                .andExpect(status().isOk()).andExpect(model().attribute("assignments",hasSize(2)))
                .andExpect(content().string(containsString("Casey Gray")))
                .andExpect(content().string(containsString("06:00 (+1 day)")));
        mvc.perform(get("/rota").with(user("presenter")).param("date","2028-01-01"))
                .andExpect(status().isOk()).andExpect(model().attribute("assignments",hasSize(0)));
        mvc.perform(get("/rota").with(user("presenter")).param("date","2026-02-30")).andExpect(status().isBadRequest());
        mvc.perform(get("/rota").with(user("presenter")).param("shift","EARLY")).andExpect(status().isBadRequest());
        mvc.perform(get("/maintenance").with(user("presenter")).param("status","UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(post("/maintenance").with(user("presenter")).with(csrf())).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/rota").with(user("presenter")).with(csrf())).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void oracleHoldsOnlyHashedCredentialsAndTheApprovedOperationalCounts() throws Exception {
        java.util.List<OperationsRecords.PlantSummary> summaries = operations.plantSummaries();
        assertEquals(160,summaries.stream().mapToInt(p -> p.total).sum());
        assertEquals(124,summaries.stream().mapToInt(p -> p.running).sum());
        assertEquals(24,summaries.stream().mapToInt(p -> p.attention).sum());
        assertEquals(12,summaries.stream().mapToInt(p -> p.stopped).sum());
        try (Connection read = datasource.getConnection(); Statement statement = read.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT password_hash FROM CALDOVA_OWNER.app_user WHERE username='presenter'")) {
                assertTrue(rs.next());
                String hash = rs.getString(1);
                assertTrue(hash.startsWith("$2"));
                assertFalse(hash.equals(password()));
                assertTrue(encoder.matches(password(),hash));
            }
            for (String expression : new String[]{"employee:16","shift_assignment:48","maintenance_work:12","app_user:1"}) {
                String[] parts = expression.split(":");
                try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM CALDOVA_OWNER."+parts[0])) {
                    assertTrue(rs.next()); assertEquals(Integer.parseInt(parts[1]),rs.getInt(1));
                }
            }
            java.sql.SQLException denied = assertThrows(java.sql.SQLException.class,
                    () -> statement.executeUpdate("UPDATE CALDOVA_OWNER.app_user SET enabled=enabled WHERE username='presenter'"));
            assertTrue(denied.getErrorCode()==41900 || denied.getErrorCode()==1031);
        }
    }

    private static String password() {
        String password = System.getenv("PRESENTER_PASSWORD");
        if (password==null || password.isEmpty()) { throw new IllegalStateException("Presenter test credentials are required."); }
        return password;
    }
}
