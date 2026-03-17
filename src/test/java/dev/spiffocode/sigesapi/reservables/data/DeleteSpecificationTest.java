package dev.spiffocode.sigesapi.reservables.data;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.WithMockCustomUser;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.AvailabilityRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.BuildingSpecifications;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;

@DataTestClass
public class DeleteSpecificationTest {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private EntityManager em;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;


    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void should_soft_delete_equipment(){

        Session session = em.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
        Availability av = Availability.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(20, 0))
                .dateFrom(LocalDate.now())
                .dateTo(LocalDate.now())
                .dayOfWeek(DayOfWeek.MONDAY)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .members(List.of(av))
                .build();
        av.setGroup(slot);

        InventoryItem inventoryItem = new InventoryItem("INV-1002");
        Equipment eq = Equipment.builder()
                .inventoryItem(inventoryItem)
                .name("Proyector Epson")
                .description("Proyector para clases")
                .studentsAvailable(true)
                .availability(List.of(slot))
                .createdAt(LocalDateTime.now())
                .createdBy("admin@test.com")
                .build();
        slot.setReservable(eq);
        equipmentRepository.save(eq);
        assertThat(equipmentRepository.findAll()).size().isEqualTo(1);
        assertThat(availabilityRepository.findAll()).size().isEqualTo(1);
        equipmentRepository.softDeleteById(eq.getId());
        assertThat(buildingRepository.findAll()).size().isEqualTo(0);
        assertThat(availabilityRepository.findAll()).size().isEqualTo(1);
        assertThat(equipmentRepository.findAllDeleted()).size().isEqualTo(1);
    }

    @Test
    void should_soft_delete_building(){

        Session session = em.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
        Building building = Building.builder()
                .name("D4")
                .build();
        buildingRepository.save(building);
        assertThat(buildingRepository.findAll()).size().isEqualTo(1);
        buildingRepository.softDeleteById(building.getId());
        assertThat(buildingRepository.findAll()).size().isEqualTo(0);
    }

    @Test
    void specification_should_return_deleted_records() {
        Building building = Building.builder()
                .name("D4")
                .build();
        buildingRepository.save(building);
        buildingRepository.softDeleteById(building.getId());
        Session session = em.unwrap(Session.class);
        try {
            session.disableFilter("softDeleteFilter");
            assertWith(buildingRepository.findAll(BuildingSpecifications.onlyDeleted()), deleted -> {
                        assertThat(deleted).size().isEqualTo(1);
                        assertThat(deleted.stream().map(Building::getDeletedAt).toList()).doesNotContainNull();
                    });
        } finally {
            session.enableFilter("softDeleteFilter");
        }
    }
}
