package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Availability;
import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilityException;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.InventoryItem;
import dev.spiffocode.sigesapi.reservables.domain.repository.AvailabilityExceptionRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.AvailabilityRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.InventoryItemRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTestClass
@Transactional
class AvailabilityValidationIT extends FlushedIntegrationTest {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AvailabilityExceptionRepository availabilityExceptionRepository;

    @Autowired
    private ReservableRepository reservableRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private Equipment equipment;

    @BeforeEach
    void setUp() {
        InventoryItem item = InventoryItem.builder()
                .inventoryNum("INV-VAL-" + System.currentTimeMillis())
                .build();

        equipment = Equipment.builder()
                .name("Test Equipment")
                .inventoryItem(item)
                .createdBy("system")
                .build();
        reservableRepository.save(equipment);
    }

    @Test
    void shouldFailIfAvailabilityStartTimeIsAfterEndTime() {

        Availability availability = Availability.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(10, 0))
                .dateFrom(LocalDate.now())
                .build();

        assertThatThrownBy(() -> availabilityRepository.saveAndFlush(availability))
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void shouldFailIfExceptionStartTimeIsAfterEndTime() {
        AvailabilityException exception = AvailabilityException.builder()
                .reservable(equipment)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(10, 0))
                .dateFrom(LocalDate.now())
                .dateTo(LocalDate.now().plusDays(1))
                .reason("Reason")
                .build();

        assertThatThrownBy(() -> availabilityExceptionRepository.saveAndFlush(exception))
                .hasMessageContaining("Start time must be before end time");
    }
}
