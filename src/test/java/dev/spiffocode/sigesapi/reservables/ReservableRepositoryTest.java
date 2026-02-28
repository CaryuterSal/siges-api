package dev.spiffocode.sigesapi.reservables;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataTestClass
class ReservableRepositoryTest {

    @Autowired ReservableRepository reservableRepository;
    @Autowired SpaceRepository spaceRepository;
    @Autowired BuildingRepository buildingRepository;
    @Autowired SpaceTypeRepository spaceTypeRepository;

    @Test
    void shouldFindSpaceByBuilding() {
        Building building = Building.builder()
                .name("Docencia 1")
                .build();
        buildingRepository.save(building);

        SpaceType tipoAula = SpaceType.builder()
                .name("Aula")
                .description("Salón de clasees")
                .build();
        spaceTypeRepository.save(tipoAula);

        Space aula = Space.builder()
                .name("Aula 1")
                .description("Aula grande para presentaciones")
                .bookInAdvance(Duration.ofSeconds(1))
                .capacity(20)
                .studentsAvailable(true)
                .building(building)
                .type(tipoAula)
                .build();
        spaceRepository.save(aula);

        List<Space> spaces = spaceRepository.findByBuildingId(building.getId());

        assertThat(spaces).hasSize(1);
        assertThat(spaces.getFirst().getDescription()).isEqualTo("Aula grande para presentaciones");
    }

    @Test
    void polymorphism_shouldFindSpaceUsingReservableRepo() {
        Building building = Building.builder()
                .name("Docencia 4")
                .build();
        buildingRepository.save(building);

        SpaceType tipoLab = SpaceType.builder()
                .name("Compu Aula")
                .description("Laboratorio de Computo")
                .build();
        spaceTypeRepository.save(tipoLab);

        Space lab = Space.builder()
                .name("CA1")
                .description("Aula de computadoras")
                .bookInAdvance(Duration.ofMinutes(30))
                .capacity(15)
                .status(ReservableStatus.MAINTENANCE)
                .studentsAvailable(false)
                .building(building)
                .type(tipoLab)
                .build();
        spaceRepository.save(lab);
        List<Reservable> result = reservableRepository.findAll();
        assertThat(result).extracting(Reservable::getStatus)
                .contains(ReservableStatus.MAINTENANCE);
    }

}
