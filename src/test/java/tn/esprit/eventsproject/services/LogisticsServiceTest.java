package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {
    @Mock LogisticsRepository logisticsRepository;
    @Mock EventRepository eventRepository;
    @InjectMocks LogisticsService service;
    private Logistics item;
    private Event event;
    private final LocalDate start = LocalDate.of(2026, 10, 1);
    private final LocalDate end = LocalDate.of(2026, 10, 31);

    @BeforeEach
    void setUp() {
        item = new Logistics(0, "Projector", true, 25f, 2);
        event = new Event();
        event.setIdEvent(10);
    }

    @Test
    void persistsItemBeforeSavingAssociation() {
        when(eventRepository.findByDescription("Conference")).thenReturn(event);
        when(logisticsRepository.save(item)).thenAnswer(invocation -> {
            item.setIdLog(42);
            return item;
        });
        assertThat(service.addAndAssign(item, " Conference ")).isSameAs(item);
        assertThat(event.getLogistics()).containsExactly(item);
        org.mockito.InOrder order = inOrder(logisticsRepository, eventRepository);
        order.verify(logisticsRepository).save(item);
        order.verify(eventRepository).save(event);
    }

    @Test
    void preservesExistingAssignments() {
        Logistics existing = new Logistics(1, "Chairs", true, 2, 10);
        event.setLogistics(new HashSet<>(Collections.singleton(existing)));
        when(eventRepository.findByDescription("Conference")).thenReturn(event);
        when(logisticsRepository.save(item)).thenReturn(item);
        service.addAndAssign(item, "Conference");
        assertThat(event.getLogistics()).containsExactlyInAnyOrder(existing, item);
        verify(eventRepository).save(event);
    }

    @Test
    void missingEventDoesNotSaveAnything() {
        assertThatThrownBy(() -> service.addAndAssign(item, "Missing"))
                .isInstanceOf(LogisticsEventNotFoundException.class);
        verifyNoInteractions(logisticsRepository);
        verify(eventRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({"0, 10", "-1, 10", "1, -0.1", "1, NaN", "1, Infinity"})
    void rejectsInvalidQuantityAndPrice(int quantity, float price) {
        item.setQuantite(quantity);
        item.setPrixUnit(price);
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, "Conference"));
        verifyNoInteractions(logisticsRepository, eventRepository);
    }

    @Test
    void rejectsMissingDescriptionsAndExistingIds() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(null, "Conference"));
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, null));
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, " "));
        item.setDescription(null);
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, "Conference"));
        item.setDescription(" ");
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, "Conference"));
        item.setDescription("Projector");
        item.setIdLog(99);
        assertThatIllegalArgumentException().isThrownBy(() -> service.addAndAssign(item, "Conference"));
        verifyNoInteractions(logisticsRepository, eventRepository);
    }

    @Test
    void allowsFreeLogistics() {
        item.setPrixUnit(0);
        when(eventRepository.findByDescription("Conference")).thenReturn(event);
        when(logisticsRepository.save(item)).thenReturn(item);
        assertThat(service.addAndAssign(item, "Conference").getPrixUnit()).isZero();
    }

    @Test
    void skipsEmptyEventsAndUnreservedItems() {
        Event empty = new Event();
        empty.setLogistics(Collections.emptySet());
        Event uninitialized = new Event();
        Logistics unreserved = new Logistics(2, "Unused", false, 5, 2);
        event.setLogistics(new HashSet<>(Arrays.asList(item, unreserved)));
        when(eventRepository.findByDateDebutBetween(start, end))
                .thenReturn(Arrays.asList(empty, uninitialized, event));
        assertThat(service.findReservedBetween(start, end)).containsExactly(item);
    }

    @Test
    void noEventsReturnsEmptyList() {
        when(eventRepository.findByDateDebutBetween(start, end)).thenReturn(Collections.emptyList());
        assertThat(service.findReservedBetween(start, end)).isEmpty();
    }

    @Test
    void rejectsInvalidIntervalsBeforeQuerying() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.findReservedBetween(null, end));
        assertThatIllegalArgumentException().isThrownBy(() -> service.findReservedBetween(start, null));
        assertThatIllegalArgumentException().isThrownBy(() -> service.findReservedBetween(end, start));
        verifyNoInteractions(logisticsRepository, eventRepository);
    }

    @Test
    void acceptsSingleDayInterval() {
        when(eventRepository.findByDateDebutBetween(start, start)).thenReturn(Collections.emptyList());
        assertThat(service.findReservedBetween(start, start)).isEmpty();
        verify(eventRepository).findByDateDebutBetween(start, start);
    }
}
