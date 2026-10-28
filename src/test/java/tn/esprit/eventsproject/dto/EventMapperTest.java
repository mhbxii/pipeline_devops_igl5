package tn.esprit.eventsproject.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    @Test
    @DisplayName("Test Participant mapping toEntity and toDTO")
    void testParticipantMapping() {
        ParticipantDTO dto = ParticipantDTO.builder()
                .idPart(1)
                .nom("Ben Salah")
                .prenom("Ali")
                .tache(Tache.ORGANISATEUR)
                .build();

        Participant entity = EventMapper.toParticipantEntity(dto);
        assertNotNull(entity);
        assertEquals(1, entity.getIdPart());
        assertEquals("Ben Salah", entity.getNom());
        assertEquals("Ali", entity.getPrenom());
        assertEquals(Tache.ORGANISATEUR, entity.getTache());

        ParticipantDTO mappedBack = EventMapper.toParticipantDTO(entity);
        assertNotNull(mappedBack);
        assertEquals(dto.getIdPart(), mappedBack.getIdPart());
        assertEquals(dto.getNom(), mappedBack.getNom());
        assertEquals(dto.getPrenom(), mappedBack.getPrenom());
        assertEquals(dto.getTache(), mappedBack.getTache());
    }

    @Test
    @DisplayName("Test Logistics mapping toEntity and toDTO")
    void testLogisticsMapping() {
        LogisticsDTO dto = LogisticsDTO.builder()
                .idLog(5)
                .description("Projecteur 4K")
                .reserve(true)
                .prixUnit(150.0f)
                .quantite(2)
                .build();

        Logistics entity = EventMapper.toLogisticsEntity(dto);
        assertNotNull(entity);
        assertEquals(5, entity.getIdLog());
        assertEquals("Projecteur 4K", entity.getDescription());
        assertTrue(entity.isReserve());
        assertEquals(150.0f, entity.getPrixUnit());
        assertEquals(2, entity.getQuantite());

        LogisticsDTO mappedBack = EventMapper.toLogisticsDTO(entity);
        assertNotNull(mappedBack);
        assertEquals(dto.getIdLog(), mappedBack.getIdLog());
        assertEquals(dto.getDescription(), mappedBack.getDescription());
        assertEquals(dto.isReserve(), mappedBack.isReserve());
        assertEquals(dto.getPrixUnit(), mappedBack.getPrixUnit());
        assertEquals(dto.getQuantite(), mappedBack.getQuantite());

        List<LogisticsDTO> list = EventMapper.toLogisticsDTOList(Collections.singletonList(entity));
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Test Event mapping toEntity and toDTO")
    void testEventMapping() {
        ParticipantDTO pDto = ParticipantDTO.builder().idPart(10).nom("Fezai").prenom("Amine").tache(Tache.SERVEUR).build();
        LogisticsDTO lDto = LogisticsDTO.builder().idLog(20).description("Chaises").reserve(false).prixUnit(5f).quantite(100).build();

        Set<ParticipantDTO> participants = new HashSet<>();
        participants.add(pDto);

        Set<LogisticsDTO> logistics = new HashSet<>();
        logistics.add(lDto);

        EventDTO eventDTO = EventDTO.builder()
                .idEvent(100)
                .description("Conférence DevOps")
                .dateDebut(LocalDate.of(2026, 12, 1))
                .dateFin(LocalDate.of(2026, 12, 3))
                .cout(2500f)
                .participants(participants)
                .logistics(logistics)
                .build();

        Event entity = EventMapper.toEventEntity(eventDTO);
        assertNotNull(entity);
        assertEquals(100, entity.getIdEvent());
        assertEquals("Conférence DevOps", entity.getDescription());
        assertEquals(1, entity.getParticipants().size());
        assertEquals(1, entity.getLogistics().size());

        EventDTO mappedBack = EventMapper.toEventDTO(entity);
        assertNotNull(mappedBack);
        assertEquals(eventDTO.getIdEvent(), mappedBack.getIdEvent());
        assertEquals(eventDTO.getDescription(), mappedBack.getDescription());
        assertEquals(1, mappedBack.getParticipants().size());
        assertEquals(1, mappedBack.getLogistics().size());

        List<EventDTO> list = EventMapper.toEventDTOList(Collections.singletonList(entity));
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Test null mappings handle gracefully")
    void testNullMappings() {
        assertNull(EventMapper.toParticipantEntity(null));
        assertNull(EventMapper.toParticipantDTO(null));
        assertNull(EventMapper.toLogisticsEntity(null));
        assertNull(EventMapper.toLogisticsDTO(null));
        assertTrue(EventMapper.toLogisticsDTOList(null).isEmpty());
        assertNull(EventMapper.toEventEntity(null));
        assertNull(EventMapper.toEventDTO(null));
        assertTrue(EventMapper.toEventDTOList(null).isEmpty());
    }
}
