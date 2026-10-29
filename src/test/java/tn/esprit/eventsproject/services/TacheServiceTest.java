package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheServiceTest {
    @Mock ParticipantRepository repository;
    TacheService service;
    Participant participant;

    @BeforeEach void setup() {
        service = new TacheService(repository);
        participant = new Participant();
        participant.setIdPart(7);
    }

    @ParameterizedTest @EnumSource(Tache.class)
    void assignsEverySupportedRole(Tache role) {
        when(repository.findById(7)).thenReturn(Optional.of(participant));
        assertThat(service.assignRole(7, role)).isEqualTo(role);
        assertThat(participant.getTache()).isEqualTo(role);
        verify(repository).save(participant);
        verify(repository, never()).delete(any());
    }

    @Test void replacesExistingRole() {
        participant.setTache(Tache.INVITE);
        when(repository.findById(7)).thenReturn(Optional.of(participant));
        service.assignRole(7, Tache.ORGANISATEUR);
        assertThat(participant.getTache()).isEqualTo(Tache.ORGANISATEUR);
    }

    @Test void readsRoleWithoutWriting() {
        participant.setTache(Tache.SERVEUR);
        when(repository.findById(7)).thenReturn(Optional.of(participant));
        assertThat(service.getRole(7)).isEqualTo(Tache.SERVEUR);
        verify(repository, never()).save(any());
    }

    @Test void clearsRoleWithoutDeletingParticipantAndIsIdempotent() {
        participant.setTache(Tache.ANIMATEUR);
        when(repository.findById(7)).thenReturn(Optional.of(participant));
        service.clearRole(7);
        service.clearRole(7);
        assertThat(participant.getTache()).isNull();
        verify(repository, times(2)).save(participant);
        verify(repository, never()).delete(any());
    }

    @Test void rejectsNullBeforeAccessingDatabase() {
        assertThatThrownBy(() -> service.assignRole(7, null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test void rejectsNonPositiveIdBeforeAccessingDatabase() {
        assertThatThrownBy(() -> service.getRole(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.clearRole(-1)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test void missingParticipantNeverWrites() {
        when(repository.findById(7)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.assignRole(7,Tache.INVITE)).isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test void propagatesPersistenceFailure() {
        when(repository.findById(7)).thenReturn(Optional.of(participant));
        when(repository.save(participant)).thenThrow(new IllegalStateException("simulated storage failure"));
        assertThatThrownBy(() -> service.assignRole(7,Tache.INVITE)).isInstanceOf(IllegalStateException.class);
    }
}
