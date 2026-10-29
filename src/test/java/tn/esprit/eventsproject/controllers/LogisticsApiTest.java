package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.services.IEventServices;
import tn.esprit.eventsproject.services.LogisticsEventNotFoundException;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventRestController.class)
@ActiveProfiles("test")
class LogisticsApiTest {
    @Autowired MockMvc mvc;
    @MockBean IEventServices services;
    private final String body = "{\"description\":\"Projector\",\"reserve\":true,\"prixUnit\":25,\"quantite\":2}";

    @Test
    void assignmentReturnsSavedLogistics() throws Exception {
        when(services.addAffectLog(any(), eq("Conference")))
                .thenReturn(new Logistics(12, "Projector", true, 25, 2));
        mvc.perform(put("/event/addAffectLog/Conference").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idLog").value(12));
    }

    @Test
    void missingEventReturns404() throws Exception {
        when(services.addAffectLog(any(), anyString())).thenThrow(new LogisticsEventNotFoundException());
        mvc.perform(put("/event/addAffectLog/Missing").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").exists());
    }

    @Test
    void invalidInputReturns400() throws Exception {
        when(services.addAffectLog(any(), anyString())).thenThrow(new IllegalArgumentException("Invalid logistics"));
        mvc.perform(put("/event/addAffectLog/Conference").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid logistics"));
    }

    @Test
    void parsesIsoDatesAndReturnsEmptyArray() throws Exception {
        when(services.getLogisticsDates(any(), any())).thenReturn(Collections.emptyList());
        mvc.perform(get("/event/getLogs/2026-10-01/2026-10-31"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(services).getLogisticsDates(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    @Test
    void malformedDateReturns400BeforeCallingService() throws Exception {
        mvc.perform(get("/event/getLogs/invalid/2026-10-31")).andExpect(status().isBadRequest());
        verifyNoInteractions(services);
    }

    @Test
    void reversedIntervalReturns400() throws Exception {
        when(services.getLogisticsDates(any(), any())).thenThrow(new IllegalArgumentException("Invalid interval"));
        mvc.perform(get("/event/getLogs/2026-10-31/2026-10-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void databaseFailureReturnsSafe500Response() throws Exception {
        when(services.addAffectLog(any(), anyString()))
                .thenThrow(new DataAccessResourceFailureException("Internal connection details"));
        mvc.perform(put("/event/addAffectLog/Conference").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database operation failed"));
    }
}
