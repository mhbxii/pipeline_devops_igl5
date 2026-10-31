package tn.esprit.eventsproject.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.controllers.dto.EventResponses.*;
import tn.esprit.eventsproject.entities.*;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class EventResponsesTest {
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test void cyclicRelationshipsBecomeIdentifierOnlyReferences() {
        Participant participant = new Participant();
        participant.setIdPart(7);
        participant.setNom("Demo");
        participant.setPrenom("User");
        participant.setTache(Tache.SERVEUR);
        Event event = new Event();
        event.setIdEvent(10);
        event.setDescription("Launch");
        event.setDateDebut(LocalDate.of(2026, 11, 5));
        event.setDateFin(LocalDate.of(2026, 11, 6));
        event.setCout(50f);
        Logistics item = new Logistics(12, "Chairs", true, 12.5f, 4);
        event.setLogistics(Collections.singleton(item));
        event.setParticipants(Collections.singleton(participant));
        participant.setEvents(Collections.singleton(event));

        JsonNode person = json.valueToTree(ParticipantResponse.from(participant));
        assertThat(person.get("idPart").asInt()).isEqualTo(7);
        assertThat(person.get("nom").asText()).isEqualTo("Demo");
        assertThat(person.get("prenom").asText()).isEqualTo("User");
        assertThat(person.get("tache").asText()).isEqualTo("SERVEUR");
        assertThat(person.at("/events/0").size()).isEqualTo(1);
        assertThat(person.at("/events/0/idEvent").asInt()).isEqualTo(10);

        EventResponse response = EventResponse.from(event);
        assertThat(response.getDateDebut()).isEqualTo(event.getDateDebut());
        assertThat(response.getDateFin()).isEqualTo(event.getDateFin());
        JsonNode result = json.valueToTree(response);
        assertThat(result.get("idEvent").asInt()).isEqualTo(10);
        assertThat(result.get("description").asText()).isEqualTo("Launch");
        assertThat(result.get("cout").asDouble()).isEqualTo(50d);
        assertThat(result.at("/participants/0").size()).isEqualTo(1);
        assertThat(result.at("/participants/0/idPart").asInt()).isEqualTo(7);
        assertThat(result.at("/logistics/0").size()).isEqualTo(1);
        assertThat(result.at("/logistics/0/idLog").asInt()).isEqualTo(12);
        JsonNode logistics = json.valueToTree(LogisticsResponse.from(item));
        assertThat(logistics)
            .isEqualTo(json.createObjectNode().put("idLog", 12).put("description", "Chairs")
                .put("reserve", true).put("prixUnit", 12.5f).put("quantite", 4));
    }

    @Test void nullAndEmptyRelationshipsRetainTheirExistingJsonShapes() {
        Participant participant = new Participant();
        Event event = new Event();
        assertThat(json.valueToTree(ParticipantResponse.from(participant)).get("events").isNull()).isTrue();
        JsonNode emptyEvent = json.valueToTree(EventResponse.from(event));
        assertThat(emptyEvent.get("participants").isNull()).isTrue();
        assertThat(emptyEvent.get("logistics").isNull()).isTrue();
        participant.setEvents(Collections.emptySet());
        event.setParticipants(Collections.emptySet());
        event.setLogistics(Collections.emptySet());
        assertThat(json.valueToTree(ParticipantResponse.from(participant)).get("events").isEmpty()).isTrue();
        emptyEvent = json.valueToTree(EventResponse.from(event));
        assertThat(emptyEvent.get("participants").isEmpty()).isTrue();
        assertThat(emptyEvent.get("logistics").isEmpty()).isTrue();
    }
}
