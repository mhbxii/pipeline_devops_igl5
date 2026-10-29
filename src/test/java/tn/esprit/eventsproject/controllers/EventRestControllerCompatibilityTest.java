package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.services.IEventServices;
import java.time.LocalDate;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventRestController.class)
class EventRestControllerCompatibilityTest {
    @Autowired MockMvc mvc;
    @MockBean IEventServices service;

    @Test void legacyParticipantJsonStillCreatesParticipant() throws Exception {
        when(service.addParticipant(any())).thenAnswer(call -> {
            Participant participant=call.getArgument(0);
            assertThat(participant.getIdPart()).isZero();
            participant.setIdPart(7);
            return participant;
        });
        mvc.perform(post("/event/addPart").contentType(MediaType.APPLICATION_JSON)
            .content("{\"idPart\":999,\"nom\":\"Demo\",\"prenom\":\"User\",\"tache\":\"INVITE\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.idPart").value(7))
            .andExpect(jsonPath("$.nom").value("Demo")).andExpect(jsonPath("$.tache").value("INVITE"));
    }

    @Test void legacyEventByParticipantRoutePreservesFieldsAndPathId() throws Exception {
        when(service.addAffectEvenParticipant(any(Event.class),eq(7))).thenAnswer(call -> {
            Event event=call.getArgument(0);
            assertThat(event.getIdEvent()).isZero();
            assertThat(event.getCout()).isZero();
            event.setIdEvent(10);
            return event;
        });
        mvc.perform(post("/event/addEvent/7").contentType(MediaType.APPLICATION_JSON)
            .content("{\"idEvent\":999,\"cout\":999,\"description\":\"Launch\",\"dateDebut\":\"2026-11-05\",\"dateFin\":\"2026-11-06\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.idEvent").value(10))
            .andExpect(jsonPath("$.description").value("Launch")).andExpect(jsonPath("$.dateDebut").value("2026-11-05"));
        verify(service).addAffectEvenParticipant(any(Event.class),eq(7));
    }

    @Test void legacyParticipantReferencesArePreservedWithoutNestedMutation() throws Exception {
        when(service.addAffectEvenParticipant(any(Event.class))).thenAnswer(call -> {
            Event event=call.getArgument(0);
            Participant reference=event.getParticipants().iterator().next();
            assertThat(reference.getIdPart()).isEqualTo(7);
            assertThat(reference.getNom()).isNull();
            return event;
        });
        mvc.perform(post("/event/addEvent").contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":\"Launch\",\"participants\":[{\"idPart\":7,\"nom\":\"untrusted\"}]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.participants[0].idPart").value(7));
    }

    @Test void legacyLogisticsRoutePreservesEditableFields() throws Exception {
        when(service.addAffectLog(any(Logistics.class),eq("Launch"))).thenAnswer(call -> {
            Logistics item=call.getArgument(0);
            assertThat(item.getIdLog()).isZero();
            item.setIdLog(12);
            return item;
        });
        mvc.perform(put("/event/addAffectLog/Launch").contentType(MediaType.APPLICATION_JSON)
            .content("{\"idLog\":999,\"description\":\"Chairs\",\"reserve\":true,\"prixUnit\":12.5,\"quantite\":4}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.idLog").value(12))
            .andExpect(jsonPath("$.reserve").value(true)).andExpect(jsonPath("$.prixUnit").value(12.5))
            .andExpect(jsonPath("$.quantite").value(4));
    }

    @ParameterizedTest @ValueSource(strings={"{\"events\":[{\"idEvent\":0}]}","{\"tache\":\"UNKNOWN\"}"})
    void invalidInputsNeverReachTheLegacyService(String body) throws Exception {
        mvc.perform(post("/event/addPart").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test void existingDateQueryContractIsPreserved() throws Exception {
        LocalDate start=LocalDate.of(2026,11,5);
        LocalDate end=LocalDate.of(2026,11,6);
        when(service.getLogisticsDates(start,end)).thenReturn(Collections.emptyList());
        mvc.perform(get("/event/getLogs/2026-11-05/2026-11-06")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).getLogisticsDates(start,end);
    }
}
