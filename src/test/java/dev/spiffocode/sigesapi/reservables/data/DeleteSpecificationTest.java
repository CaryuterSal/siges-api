package dev.spiffocode.sigesapi.reservables.data;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.BuildingSpecifications;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;

@DataTestClass
public class DeleteSpecificationTest {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private EntityManager em;

    @Test
    void should_soft_delete_records(){

        Session session = em.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
        Building building = Building.builder()
                .name("D4")
                .build();
        buildingRepository.save(building);
        assertThat(buildingRepository.findAll()).size().isEqualTo(1);
        buildingRepository.delete(building);
        assertThat(buildingRepository.findAll()).size().isEqualTo(0);
    }

    @Test
    void specification_should_return_deleted_records() {
        Building building = Building.builder()
                .name("D4")
                .build();
        buildingRepository.save(building);
        buildingRepository.delete(building);

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
