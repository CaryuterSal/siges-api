package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotUpdateDto;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTestClass
public class AvailabilityMapperTest {

        private final AvailabilityMapper mapper = new AvailabilityMapperImpl();

        @Test
        void entity_to_dto() {
                InventoryItem inventoryItem = new InventoryItem("INV-123");
                Equipment equipment = Equipment.builder()
                                .id(1L)
                                .name("Projector")
                                .description("HDMI Projector")
                                .inventoryItem(inventoryItem)
                                .studentsAvailable(false)
                                .building(Building.builder().id(10L).build())
                                .space(Space.builder().id(20L).build())
                                .build();
                Availability availability = Availability.builder()
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .dateFrom(LocalDate.of(2023, 1, 1))
                                .dateTo(LocalDate.of(2023, 12, 31))
                                .build();

                AvailabilitySlot entity = AvailabilitySlot.builder()
                                .id(1L)
                                .reservable(equipment)
                                .members(List.of(availability))
                                .build();

                AvailabilitySlotDto dto = mapper.toDto(entity);

                assertNotNull(dto);
                assertEquals(entity.getId(), dto.id());
                assertEquals(equipment.getId(), dto.reservableId());
                assertEquals(availability.getStartTime(), dto.startTime());
                assertEquals(availability.getEndTime(), dto.endTime());
                assertEquals(availability.getDateFrom(), dto.dateFrom());
                assertEquals(availability.getDateTo(), dto.dateTo());
        }

        @Test
        void register_dto_to_entity() {
                AvailabilitySlotRegisterDto request = AvailabilitySlotRegisterDto.builder()
                                .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                                .dateFrom(LocalDate.of(2023, 1, 1))
                                .dateTo(LocalDate.of(2023, 12, 31))
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .build();

                AvailabilitySlot entity = mapper.toEntity(request);

                assertNotNull(entity);
                assertNotNull(entity.getMembers());
                assertEquals(2, entity.getMembers().size());

                Availability member1 = entity.getMembers().get(0);
                assertEquals(request.dateFrom(), member1.getDateFrom());
                assertEquals(request.dateTo(), member1.getDateTo());
                assertEquals(request.startTime(), member1.getStartTime());
                assertEquals(request.endTime(), member1.getEndTime());
        }

        @Test
        void update_dto_to_entity() {
                AvailabilitySlotUpdateDto request = AvailabilitySlotUpdateDto.builder()
                                .dateFrom(LocalDate.of(2024, 1, 1))
                                .dateTo(LocalDate.of(2024, 12, 31))
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(11, 0))
                                .daysOfWeek(Set.of(DayOfWeek.FRIDAY))
                                .build();

                AvailabilitySlot entity = AvailabilitySlot.builder().id(5L).build();

                mapper.updateEntity(entity, request);

                assertNotNull(entity.getMembers());
                assertEquals(1, entity.getMembers().size());

                Availability member = entity.getMembers().get(0);
                assertEquals(request.dateFrom(), member.getDateFrom());
                assertEquals(request.dateTo(), member.getDateTo());
                assertEquals(request.startTime(), member.getStartTime());
                assertEquals(request.endTime(), member.getEndTime());
                assertEquals(DayOfWeek.FRIDAY, member.getDayOfWeek());
        }
}
