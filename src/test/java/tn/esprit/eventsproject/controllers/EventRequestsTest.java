package tn.esprit.eventsproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.eventsproject.controllers.dto.EventRequests.*;
import tn.esprit.eventsproject.entities.*;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

/** Real JSON mapping and allowlists, with no mocks and no database. */
class EventRequestsTest {
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test void participantPreservesCreateFieldsButNotIdsOrNestedState() throws Exception {
        Participant participant = json.readValue("{\"idPart\":999,\"nom\":\"Demo\",\"prenom\":\"User\",\"tache\":\"SERVEUR\","
            + "\"events\":[{\"idEvent\":7,\"description\":\"untrusted\"}]}", ParticipantCreateRequest.class).toEntity();
        assertThat(participant.getIdPart()).isZero();
        assertThat(participant.getNom()).isEqualTo("Demo");
        assertThat(participant.getPrenom()).isEqualTo("User");
        assertThat(participant.getTache()).isEqualTo(Tache.SERVEUR);
        assertThat(participant.getEvents()).hasSize(1);
        Event reference = participant.getEvents().iterator().next();
        assertThat(reference.getIdEvent()).isEqualTo(7);
        assertThat(reference.getDescription()).isNull();
    }

    @Test void eventPreservesDatesAndReferencesButNotGeneratedIdCostOrNestedState() throws Exception {
        Event event = json.readValue("{\"idEvent\":999,\"cout\":9999,\"description\":\"Launch\","
            + "\"dateDebut\":\"2026-11-05\",\"dateFin\":\"2026-11-06\","
            + "\"participants\":[{\"idPart\":7,\"nom\":\"untrusted\",\"tache\":\"ORGANISATEUR\"}],"
            + "\"logistics\":[{\"idLog\":8,\"prixUnit\":9999}]}", EventCreateRequest.class).toEntity();
        assertThat(event.getIdEvent()).isZero();
        assertThat(event.getCout()).isZero();
        assertThat(event.getDescription()).isEqualTo("Launch");
        assertThat(event.getDateDebut()).isEqualTo(LocalDate.of(2026,11,5));
        assertThat(event.getDateFin()).isEqualTo(LocalDate.of(2026,11,6));
        Participant participant = event.getParticipants().iterator().next();
        assertThat(participant.getIdPart()).isEqualTo(7);
        assertThat(participant.getNom()).isNull();
        assertThat(participant.getTache()).isNull();
        Logistics item = event.getLogistics().iterator().next();
        assertThat(item.getIdLog()).isEqualTo(8);
        assertThat(item.getPrixUnit()).isZero();
    }

    @Test void logisticsPreservesEditableFieldsButNotGeneratedId() throws Exception {
        Logistics item = json.readValue("{\"idLog\":999,\"description\":\"Chairs\",\"reserve\":true,\"prixUnit\":12.5,\"quantite\":4}",
            LogisticsCreateRequest.class).toEntity();
        assertThat(item.getIdLog()).isZero();
        assertThat(item.getDescription()).isEqualTo("Chairs");
        assertThat(item.isReserve()).isTrue();
        assertThat(item.getPrixUnit()).isEqualTo(12.5f);
        assertThat(item.getQuantite()).isEqualTo(4);
    }

    @Test void omittedRelationshipsRemainEmpty() {
        assertThat(new ParticipantCreateRequest().toEntity().getEvents()).isEmpty();
        Event event = new EventCreateRequest().toEntity();
        assertThat(event.getParticipants()).isEmpty();
        assertThat(event.getLogistics()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings={"{\"events\":[{\"idEvent\":0}]}","{\"events\":[{\"idEvent\":-2}]}","{\"events\":[null]}"})
    void invalidEventReferencesAreRejected(String body) {
        assertThatThrownBy(() -> json.readValue(body, ParticipantCreateRequest.class).toEntity())
            .isInstanceOf(ResponseStatusException.class);
    }

    @ParameterizedTest
    @ValueSource(strings={"{\"participants\":[{\"idPart\":0}]}","{\"logistics\":[{\"idLog\":0}]}",
        "{\"participants\":[null]}","{\"logistics\":[null]}"})
    void invalidParticipantOrLogisticsReferencesAreRejected(String body) {
        assertThatThrownBy(() -> json.readValue(body, EventCreateRequest.class).toEntity())
            .isInstanceOf(ResponseStatusException.class);
    }
}
