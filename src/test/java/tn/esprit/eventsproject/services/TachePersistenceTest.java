package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.*;

/** Integration test: real JPA/Hibernate and H2, no mocks. Not a MySQL compatibility test. */
@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(TacheService.class)
class TachePersistenceTest {
    @Autowired ParticipantRepository repository;
    @Autowired TacheService service;
    @Autowired EntityManager entityManager;

    @Test void persistsReadsReplacesAndClearsRoleWithoutRemovingParticipant() {
        Participant participant = new Participant();
        participant.setNom("Integration");
        int id = repository.saveAndFlush(participant).getIdPart();
        service.assignRole(id,Tache.ORGANISATEUR);
        entityManager.flush(); entityManager.clear();
        assertThat(service.getRole(id)).isEqualTo(Tache.ORGANISATEUR);
        assertThat(entityManager.createNativeQuery("select tache from participant where id_part = :id")
            .setParameter("id",id).getSingleResult()).isEqualTo("ORGANISATEUR");
        service.assignRole(id,Tache.SERVEUR);
        entityManager.flush(); entityManager.clear();
        assertThat(service.getRole(id)).isEqualTo(Tache.SERVEUR);
        service.clearRole(id);
        entityManager.flush(); entityManager.clear();
        assertThat(service.getRole(id)).isNull();
        assertThat(repository.findById(id)).isPresent();
        assertThat(repository.findById(id).get().getNom()).isEqualTo("Integration");
    }
}
