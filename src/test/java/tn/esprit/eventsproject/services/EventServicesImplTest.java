package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.repositories.EventRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServicesImplTest {

    @Autowired
    private IEventServices eventServices;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Test addEvent - Sans Mock (Persistance réelle en base)")
    void testAddEvent_withoutMock() {
        Event event = Event.builder()
                .description("Hackathon DevOps 2026")
                .dateDebut(LocalDate.of(2026, 11, 15))
                .dateFin(LocalDate.of(2026, 11, 17))
                .cout(3500.0f)
                .build();

        Event savedEvent = eventServices.addEvent(event);

        assertNotNull(savedEvent, "L'événement sauvegardé ne doit pas être null");
        assertTrue(savedEvent.getIdEvent() > 0, "L'ID de l'événement doit être généré");
        assertEquals("Hackathon DevOps 2026", savedEvent.getDescription());

        // Vérification de la persistance réelle
        Event found = eventRepository.findById(savedEvent.getIdEvent()).orElse(null);
        assertNotNull(found);
        assertEquals(3500.0f, found.getCout());
    }

    @Test
    @DisplayName("Test retrieveEvent - Sans Mock")
    void testRetrieveEvent_withoutMock() {
        Event event = Event.builder()
                .description("Séminaire Intelligence Artificielle")
                .dateDebut(LocalDate.of(2026, 10, 5))
                .dateFin(LocalDate.of(2026, 10, 6))
                .cout(1200f)
                .build();
        Event saved = eventRepository.save(event);

        Event retrieved = eventServices.retrieveEvent(saved.getIdEvent());

        assertNotNull(retrieved);
        assertEquals(saved.getIdEvent(), retrieved.getIdEvent());
        assertEquals("Séminaire Intelligence Artificielle", retrieved.getDescription());
    }

    @Test
    @DisplayName("Test retrieveAllEvents - Sans Mock")
    void testRetrieveAllEvents_withoutMock() {
        Event e1 = Event.builder()
                .description("Conférence Microservices")
                .dateDebut(LocalDate.now())
                .dateFin(LocalDate.now().plusDays(1))
                .cout(800f)
                .build();

        Event e2 = Event.builder()
                .description("Atelier Kubernetes")
                .dateDebut(LocalDate.now().plusDays(2))
                .dateFin(LocalDate.now().plusDays(3))
                .cout(1200f)
                .build();

        eventServices.addEvent(e1);
        eventServices.addEvent(e2);

        List<Event> allEvents = eventServices.retrieveAllEvents();

        assertNotNull(allEvents);
        assertTrue(allEvents.size() >= 2, "La liste doit contenir au moins les 2 événements ajoutés");
    }

    @Test
    @DisplayName("Test updateEvent - Sans Mock")
    void testUpdateEvent_withoutMock() {
        Event event = Event.builder()
                .description("Formation Jenkins initiale")
                .dateDebut(LocalDate.of(2026, 12, 1))
                .dateFin(LocalDate.of(2026, 12, 2))
                .cout(500f)
                .build();
        Event saved = eventServices.addEvent(event);

        saved.setDescription("Formation Jenkins CI/CD Avancée");
        saved.setCout(750f);
        Event updated = eventServices.updateEvent(saved);

        assertNotNull(updated);
        assertEquals("Formation Jenkins CI/CD Avancée", updated.getDescription());
        assertEquals(750f, updated.getCout());

        Event verifiedFromDb = eventRepository.findById(saved.getIdEvent()).orElse(null);
        assertNotNull(verifiedFromDb);
        assertEquals("Formation Jenkins CI/CD Avancée", verifiedFromDb.getDescription());
    }

    @Test
    @DisplayName("Test deleteEvent - Sans Mock")
    void testDeleteEvent_withoutMock() {
        Event event = Event.builder()
                .description("Événement temporaire à supprimer")
                .dateDebut(LocalDate.now())
                .dateFin(LocalDate.now().plusDays(1))
                .cout(200f)
                .build();
        Event saved = eventServices.addEvent(event);
        int eventId = saved.getIdEvent();

        eventServices.deleteEvent(eventId);

        Event deleted = eventRepository.findById(eventId).orElse(null);
        assertNull(deleted, "L'événement doit être supprimé de la base de données");
    }

    @Test
    @DisplayName("Test logique métier Event - Sans Mock")
    void testEventEntityLogic_withoutMock() {
        Event event = new Event();
        event.setIdEvent(42);
        event.setDescription("Conférence Green IT");
        event.setDateDebut(LocalDate.of(2026, 9, 1));
        event.setDateFin(LocalDate.of(2026, 9, 3));
        event.setCout(1800.5f);

        assertEquals(42, event.getIdEvent());
        assertEquals("Conférence Green IT", event.getDescription());
        assertTrue(event.getDateFin().isAfter(event.getDateDebut()));
        assertEquals(1800.5f, event.getCout());
    }
}
