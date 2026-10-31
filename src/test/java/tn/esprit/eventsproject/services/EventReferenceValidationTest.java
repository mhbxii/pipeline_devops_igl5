package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.repositories.*;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventReferenceValidationTest {
    private final EventRepository events = mock(EventRepository.class);
    private final ParticipantRepository participants = mock(ParticipantRepository.class);
    private final LogisticsRepository logistics = mock(LogisticsRepository.class);
    private EventServicesImpl service;

    @BeforeEach void setUp() { service = new EventServicesImpl(events, participants, logistics); }

    @Test void missingParticipantByPathIsNotFoundWithoutSavingEvent() {
        when(participants.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.addAffectEvenParticipant(new Event(), 99))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(events, logistics);
    }

    @Test void missingParticipantReferenceIsNotFoundWithoutSavingEvent() {
        Participant reference = new Participant();
        reference.setIdPart(99);
        Event event = new Event();
        event.setParticipants(Collections.singleton(reference));
        when(participants.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.addAffectEvenParticipant(event))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(events, logistics);
    }

    @ParameterizedTest @ValueSource(ints = {0, -1})
    void invalidPathIdIsBadRequestBeforeDatabaseAccess(int id) {
        assertThatThrownBy(() -> service.addAffectEvenParticipant(new Event(), id))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(events, participants, logistics);
    }

    @Test void existingParticipantStillReceivesEvent() {
        Participant participant = new Participant();
        Event event = new Event();
        when(participants.findById(7)).thenReturn(Optional.of(participant));
        when(events.save(event)).thenReturn(event);
        assertThat(service.addAffectEvenParticipant(event, 7)).isSameAs(event);
        assertThat(participant.getEvents()).containsExactly(event);
    }

    @Test void missingEventForLogisticsIsNotFoundWithoutSavingLogistics() {
        when(events.findByDescription("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.addAffectLog(new Logistics(), "missing"))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(logistics, participants);
        verify(events, never()).save(any(Event.class));
    }
}
