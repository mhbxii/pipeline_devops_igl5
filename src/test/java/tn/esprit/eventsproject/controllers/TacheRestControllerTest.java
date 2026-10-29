package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.services.TacheService;
import javax.persistence.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TacheRestController.class)
class TacheRestControllerTest {
    @Autowired MockMvc mvc;
    @MockBean TacheService service;

    @Test void listsAllowedRoles() throws Exception {
        mvc.perform(get("/taches")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
    }
    @Test void unassignedRoleIsExplicitlyNull() throws Exception {
        mvc.perform(get("/taches/participants/7")).andExpect(status().isOk())
            .andExpect(content().json("{\"participantId\":7,\"tache\":null}"));
    }
    @Test void nonNumericIdIsBadRequest() throws Exception {
        mvc.perform(get("/taches/participants/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void assignsRole() throws Exception {
        when(service.assignRole(7,Tache.SERVEUR)).thenReturn(Tache.SERVEUR);
        mvc.perform(put("/taches/participants/7").contentType(MediaType.APPLICATION_JSON).content("{\"tache\":\"SERVEUR\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tache").value("SERVEUR"));
    }
    @Test void unknownEnumIsBadRequestWithoutCallingService() throws Exception {
        mvc.perform(put("/taches/participants/7").contentType(MediaType.APPLICATION_JSON).content("{\"tache\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void missingParticipantIsNotFound() throws Exception {
        when(service.getRole(99)).thenThrow(new EntityNotFoundException("Participant not found"));
        mvc.perform(get("/taches/participants/99")).andExpect(status().isNotFound());
    }
    @Test void deletingRoleReturnsNoContent() throws Exception {
        mvc.perform(delete("/taches/participants/7")).andExpect(status().isNoContent());
        verify(service).clearRole(7);
    }
    @Test void unexpectedFailureDoesNotLeakDetails() throws Exception {
        when(service.getRole(7)).thenThrow(new IllegalStateException("secret connection detail"));
        mvc.perform(get("/taches/participants/7")).andExpect(status().isInternalServerError())
            .andExpect(content().json("{\"error\":\"Role operation failed\"}"));
    }
}
