package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@Import(ParticipantServicesImpl.class)
class ParticipantServicesDbTest {

    @Autowired
    ParticipantServicesImpl participantServices;

    private Participant build(String nom) {
        Participant p = new Participant();
        p.setNom(nom);
        p.setPrenom("Test");
        p.setTache(Tache.ORGANISATEUR);
        return p;
    }

    @Test
    void addAndRetrieve() {
        Participant saved = participantServices.addParticipant(build("Tounsi"));

        assertTrue(saved.getIdPart() > 0);
        assertEquals("Tounsi", participantServices.retrieveParticipant(saved.getIdPart()).getNom());
    }

    @Test
    void retrieveAll() {
        participantServices.addParticipant(build("A"));
        participantServices.addParticipant(build("B"));

        assertEquals(2, participantServices.retrieveAllParticipants().size());
    }

    @Test
    void update() {
        Participant saved = participantServices.addParticipant(build("Old"));
        saved.setNom("New");

        assertEquals("New", participantServices.updateParticipant(saved).getNom());
    }

    @Test
    void remove() {
        Participant saved = participantServices.addParticipant(build("ToDelete"));
        int id = saved.getIdPart();

        participantServices.removeParticipant(id);

        assertThrows(EntityNotFoundException.class, () -> participantServices.retrieveParticipant(id));
    }
}