package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import(LogisticsService.class)
class LogisticsPersistenceTest {
    @Autowired LogisticsService service;
    @Autowired EventRepository events;
    @Autowired LogisticsRepository logistics;
    @Autowired TestEntityManager entityManager;
    private final LocalDate day = LocalDate.of(2026, 10, 29);

    @Test
    void assignmentSurvivesFlushAndReload() {
        Event event = createEvent("Conference", day);
        Logistics saved = service.addAndAssign(item(true), "Conference");
        entityManager.flush();
        entityManager.clear();
        assertThat(saved.getIdLog()).isPositive();
        assertThat(events.findById(event.getIdEvent()).get().getLogistics())
                .extracting(Logistics::getIdLog).containsExactly(saved.getIdLog());
        assertThat(logistics.findById(saved.getIdLog())).isPresent();
    }

    @Test
    void secondAssignmentPreservesFirstOneInDatabase() {
        Event event = createEvent("Workshop", day);
        service.addAndAssign(item(true), "Workshop");
        service.addAndAssign(item(false), "Workshop");
        entityManager.flush();
        entityManager.clear();
        assertThat(events.findById(event.getIdEvent()).get().getLogistics()).hasSize(2);
    }

    @Test
    void queryIncludesBothBoundariesAndSkipsEmptyUnreservedAndOutsideEvents() {
        LocalDate end = day.plusDays(2);
        createEvent("Empty", day);
        createEvent("Start", day);
        createEvent("End", end);
        createEvent("Before", day.minusDays(1));
        createEvent("After", end.plusDays(1));
        Logistics first = service.addAndAssign(item(true), "Start");
        Logistics last = service.addAndAssign(item(true), "End");
        service.addAndAssign(item(false), "Start");
        service.addAndAssign(item(true), "Before");
        service.addAndAssign(item(true), "After");
        entityManager.flush();
        entityManager.clear();
        assertThat(service.findReservedBetween(day, end)).extracting(Logistics::getIdLog)
                .containsExactlyInAnyOrder(first.getIdLog(), last.getIdLog());
    }

    @Test
    void missingEventLeavesNoOrphanLogistics() {
        long count = logistics.count();
        assertThatThrownBy(() -> service.addAndAssign(item(true), "Missing"))
                .isInstanceOf(LogisticsEventNotFoundException.class);
        assertThat(logistics.count()).isEqualTo(count);
    }

    @Test
    void emptyDatabaseReturnsEmptyList() {
        assertThat(service.findReservedBetween(day, day)).isEmpty();
    }

    private Event createEvent(String description, LocalDate date) {
        Event event = new Event();
        event.setDescription(description);
        event.setDateDebut(date);
        event.setDateFin(date.plusDays(1));
        return events.saveAndFlush(event);
    }

    private Logistics item(boolean reserved) {
        return new Logistics(0, "Projector", reserved, 25f, 2);
    }
}
