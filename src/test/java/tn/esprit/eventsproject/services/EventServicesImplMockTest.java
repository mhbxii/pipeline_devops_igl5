package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplMockTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = Event.builder()
                .idEvent(1)
                .description("Forum DevOps Esprit")
                .dateDebut(LocalDate.of(2026, 11, 1))
                .dateFin(LocalDate.of(2026, 11, 3))
                .cout(1500f)
                .participants(new HashSet<>())
                .logistics(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("Test addEvent - Avec Mock")
    void testAddEvent_withMock() {
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);

        Event result = eventServices.addEvent(sampleEvent);

        assertNotNull(result);
        assertEquals(1, result.getIdEvent());
        assertEquals("Forum DevOps Esprit", result.getDescription());
        verify(eventRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("Test retrieveEvent (ID existant) - Avec Mock")
    void testRetrieveEvent_Existing_withMock() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(sampleEvent));

        Event result = eventServices.retrieveEvent(1);

        assertNotNull(result);
        assertEquals("Forum DevOps Esprit", result.getDescription());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test retrieveEvent (ID non existant) - Avec Mock")
    void testRetrieveEvent_NotFound_withMock() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        Event result = eventServices.retrieveEvent(99);

        assertNull(result);
        verify(eventRepository, times(1)).findById(99);
    }

    @Test
    @DisplayName("Test retrieveAllEvents - Avec Mock")
    void testRetrieveAllEvents_withMock() {
        Event event2 = Event.builder()
                .idEvent(2)
                .description("Cloud Summit")
                .dateDebut(LocalDate.of(2026, 12, 10))
                .dateFin(LocalDate.of(2026, 12, 12))
                .cout(2500f)
                .build();

        List<Event> mockList = Arrays.asList(sampleEvent, event2);
        when(eventRepository.findAll()).thenReturn(mockList);

        List<Event> results = eventServices.retrieveAllEvents();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Cloud Summit", results.get(1).getDescription());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test updateEvent - Avec Mock")
    void testUpdateEvent_withMock() {
        when(eventRepository.existsById(sampleEvent.getIdEvent())).thenReturn(true);
        sampleEvent.setDescription("Forum DevOps Esprit - Updated");
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);

        Event updated = eventServices.updateEvent(sampleEvent);

        assertNotNull(updated);
        assertEquals("Forum DevOps Esprit - Updated", updated.getDescription());
        verify(eventRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("Test deleteEvent - Avec Mock")
    void testDeleteEvent_withMock() {
        when(eventRepository.existsById(1)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(1);

        eventServices.deleteEvent(1);

        verify(eventRepository, times(1)).existsById(1);
        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Test addAffectEvenParticipant - Avec Mock")
    void testAddAffectEvenParticipant_withMock() {
        Participant participant = new Participant();
        participant.setIdPart(10);
        participant.setNom("Ben Salah");
        participant.setPrenom("Karim");

        when(participantRepository.findById(10)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);

        Event result = eventServices.addAffectEvenParticipant(sampleEvent, 10);

        assertNotNull(result);
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(sampleEvent));
        verify(participantRepository, times(1)).findById(10);
        verify(eventRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("Test calculCout - Avec Mock")
    void testCalculCout_withMock() {
        Logistics log1 = new Logistics();
        log1.setIdLog(1);
        log1.setDescription("Chaises");
        log1.setReserve(true);
        log1.setPrixUnit(10f);
        log1.setQuantite(50); // 500f

        Logistics log2 = new Logistics();
        log2.setIdLog(2);
        log2.setDescription("Projecteur");
        log2.setReserve(false); // Non réservé -> ignoré
        log2.setPrixUnit(300f);
        log2.setQuantite(1);

        Set<Logistics> logisticsSet = new HashSet<>(Arrays.asList(log1, log2));
        sampleEvent.setLogistics(logisticsSet);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);

        eventServices.calculCout();

        assertEquals(500f, sampleEvent.getCout());
        verify(eventRepository, times(1)).save(sampleEvent);
    }
}
