package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {
    private final LogisticsRepository logisticsRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Logistics addAndAssign(Logistics logistics, String eventDescription) {
        validate(logistics, eventDescription);
        Event event = eventRepository.findByDescription(eventDescription.trim());
        if (event == null) {
            log.warn("Logistics assignment rejected: event not found");
            throw new LogisticsEventNotFoundException();
        }
        Logistics saved = logisticsRepository.save(logistics);
        if (event.getLogistics() == null) {
            event.setLogistics(new HashSet<>());
        }
        event.getLogistics().add(saved);
        eventRepository.save(event);
        log.info("Assigned logistics id={} to event id={}", saved.getIdLog(), event.getIdEvent());
        log.debug("Logistics reserved={}, quantity={}, unitPrice={}",
                saved.isReserve(), saved.getQuantite(), saved.getPrixUnit());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Logistics> findReservedBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            log.warn("Logistics lookup rejected: invalid date interval");
            throw new IllegalArgumentException("Provide an ordered date interval (yyyy-MM-dd)");
        }
        List<Logistics> result = new ArrayList<>();
        for (Event event : eventRepository.findByDateDebutBetween(start, end)) {
            if (event.getLogistics() != null) {
                for (Logistics logistics : event.getLogistics()) {
                    if (logistics.isReserve()) {
                        result.add(logistics);
                    }
                }
            }
        }
        log.info("Found {} reserved logistics between {} and {}", result.size(), start, end);
        return result;
    }

    private void validate(Logistics logistics, String eventDescription) {
        if (logistics == null || logistics.getIdLog() != 0
                || logistics.getDescription() == null || logistics.getDescription().trim().isEmpty()
                || logistics.getQuantite() <= 0 || !Float.isFinite(logistics.getPrixUnit())
                || logistics.getPrixUnit() < 0
                || eventDescription == null || eventDescription.trim().isEmpty()) {
            log.warn("Logistics assignment rejected: invalid input");
            throw new IllegalArgumentException(
                    "New logistics require a description, positive quantity, non-negative finite price and event description");
        }
    }
}
