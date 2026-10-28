package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRestControllerTest {

    @Mock
    private IEventServices eventServices;

    @InjectMocks
    private EventRestController eventRestController;

    private Event sampleEvent;
    private Participant sampleParticipant;
    private Logistics sampleLogistics;

    @BeforeEach
    void setUp() {
        sampleEvent = Event.builder()
                .idEvent(1)
                .description("DevOps Summit")
                .dateDebut(LocalDate.of(2026, 11, 1))
                .dateFin(LocalDate.of(2026, 11, 2))
                .cout(1000f)
                .build();

        sampleParticipant = new Participant();
        sampleParticipant.setIdPart(1);
        sampleParticipant.setNom("Ben Salah");
        sampleParticipant.setPrenom("Ali");
        sampleParticipant.setTache(Tache.ORGANISATEUR);

        sampleLogistics = new Logistics();
        sampleLogistics.setIdLog(1);
        sampleLogistics.setDescription("Ecran LED");
        sampleLogistics.setReserve(true);
        sampleLogistics.setPrixUnit(200f);
        sampleLogistics.setQuantite(2);
    }

    @Test
    @DisplayName("Test addParticipant endpoint")
    void testAddParticipant() {
        when(eventServices.addParticipant(any(Participant.class))).thenReturn(sampleParticipant);

        ParticipantDTO dto = ParticipantDTO.builder()
                .nom("Ben Salah")
                .prenom("Ali")
                .tache(Tache.ORGANISATEUR)
                .build();

        ParticipantDTO result = eventRestController.addParticipant(dto);

        assertNotNull(result);
        assertEquals("Ben Salah", result.getNom());
        verify(eventServices, times(1)).addParticipant(any(Participant.class));
    }

    @Test
    @DisplayName("Test addEventPart endpoint")
    void testAddEventPart() {
        when(eventServices.addAffectEvenParticipant(any(Event.class), eq(5))).thenReturn(sampleEvent);

        EventDTO dto = EventDTO.builder().description("DevOps Summit").build();
        EventDTO result = eventRestController.addEventPart(dto, 5);

        assertNotNull(result);
        assertEquals("DevOps Summit", result.getDescription());
        verify(eventServices, times(1)).addAffectEvenParticipant(any(Event.class), eq(5));
    }

    @Test
    @DisplayName("Test addEvent endpoint")
    void testAddEvent() {
        when(eventServices.addAffectEvenParticipant(any(Event.class))).thenReturn(sampleEvent);

        EventDTO dto = EventDTO.builder().description("DevOps Summit").build();
        EventDTO result = eventRestController.addEvent(dto);

        assertNotNull(result);
        assertEquals(1, result.getIdEvent());
        verify(eventServices, times(1)).addAffectEvenParticipant(any(Event.class));
    }

    @Test
    @DisplayName("Test addAffectLog endpoint")
    void testAddAffectLog() {
        when(eventServices.addAffectLog(any(Logistics.class), eq("DevOps Summit"))).thenReturn(sampleLogistics);

        LogisticsDTO dto = LogisticsDTO.builder().description("Ecran LED").prixUnit(200f).build();
        LogisticsDTO result = eventRestController.addAffectLog(dto, "DevOps Summit");

        assertNotNull(result);
        assertEquals("Ecran LED", result.getDescription());
        verify(eventServices, times(1)).addAffectLog(any(Logistics.class), eq("DevOps Summit"));
    }

    @Test
    @DisplayName("Test getLogistiquesDates endpoint")
    void testGetLogistiquesDates() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 31);
        when(eventServices.getLogisticsDates(d1, d2)).thenReturn(Collections.singletonList(sampleLogistics));

        List<LogisticsDTO> result = eventRestController.getLogistiquesDates(d1, d2);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ecran LED", result.get(0).getDescription());
    }

    @Test
    @DisplayName("Test saveEvent endpoint")
    void testSaveEvent() {
        when(eventServices.addEvent(any(Event.class))).thenReturn(sampleEvent);

        EventDTO dto = EventDTO.builder().description("DevOps Summit").build();
        EventDTO result = eventRestController.saveEvent(dto);

        assertNotNull(result);
        assertEquals("DevOps Summit", result.getDescription());
        verify(eventServices, times(1)).addEvent(any(Event.class));
    }

    @Test
    @DisplayName("Test getAllEvents endpoint")
    void testGetAllEvents() {
        when(eventServices.retrieveAllEvents()).thenReturn(Collections.singletonList(sampleEvent));

        List<EventDTO> result = eventRestController.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DevOps Summit", result.get(0).getDescription());
    }

    @Test
    @DisplayName("Test getEventById endpoint")
    void testGetEventById() {
        when(eventServices.retrieveEvent(1)).thenReturn(sampleEvent);

        EventDTO result = eventRestController.getEventById(1);

        assertNotNull(result);
        assertEquals(1, result.getIdEvent());
    }

    @Test
    @DisplayName("Test updateEvent endpoint")
    void testUpdateEvent() {
        when(eventServices.updateEvent(any(Event.class))).thenReturn(sampleEvent);

        EventDTO dto = EventDTO.builder().idEvent(1).description("Updated").build();
        EventDTO result = eventRestController.updateEvent(dto);

        assertNotNull(result);
        assertEquals(1, result.getIdEvent());
        verify(eventServices, times(1)).updateEvent(any(Event.class));
    }

    @Test
    @DisplayName("Test deleteEvent endpoint")
    void testDeleteEvent() {
        doNothing().when(eventServices).deleteEvent(1);

        eventRestController.deleteEvent(1);

        verify(eventServices, times(1)).deleteEvent(1);
    }
}
