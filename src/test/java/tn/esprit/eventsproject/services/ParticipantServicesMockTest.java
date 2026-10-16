package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServicesMockTest {

    @Mock
    ParticipantRepository participantRepository;

    @InjectMocks
    ParticipantServicesImpl participantServices;

    private Participant build(int id, String nom) {
        Participant p = new Participant();
        p.setIdPart(id);
        p.setNom(nom);
        p.setPrenom("Test");
        p.setTache(Tache.INVITE);
        return p;
    }

    @Test
    void addParticipant() {
        Participant p = build(1, "Tounsi");
        when(participantRepository.save(p)).thenReturn(p);

        Participant result = participantServices.addParticipant(p);

        assertEquals("Tounsi", result.getNom());
        verify(participantRepository, times(1)).save(p);
    }

    @Test
    void retrieveAllParticipants() {
        List<Participant> list = Arrays.asList(build(1, "A"), build(2, "B"));
        when(participantRepository.findAll()).thenReturn(list);

        assertEquals(2, participantServices.retrieveAllParticipants().size());
        verify(participantRepository).findAll();
    }

    @Test
    void retrieveParticipantFound() {
        Participant p = build(1, "A");
        when(participantRepository.findById(1)).thenReturn(Optional.of(p));

        assertEquals(1, participantServices.retrieveParticipant(1).getIdPart());
    }

    @Test
    void retrieveParticipantNotFound() {
        when(participantRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> participantServices.retrieveParticipant(99));
    }

    @Test
    void updateParticipant() {
        Participant p = build(1, "Updated");
        when(participantRepository.existsById(1)).thenReturn(true);
        when(participantRepository.save(p)).thenReturn(p);

        assertEquals("Updated", participantServices.updateParticipant(p).getNom());
    }

    @Test
    void updateParticipantNotFound() {
        Participant p = build(99, "X");
        when(participantRepository.existsById(99)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> participantServices.updateParticipant(p));
        verify(participantRepository, never()).save(any());
    }

    @Test
    void removeParticipant() {
        when(participantRepository.existsById(1)).thenReturn(true);

        participantServices.removeParticipant(1);

        verify(participantRepository, times(1)).deleteById(1);
    }
}