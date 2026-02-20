package dev.spiffocode.sigesapi.reservables;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        Building building = new Building();
        building.setName("Docencia 1");
        buildingRepository.save(building);

        SpaceType tipoAula = new SpaceType();
        tipoAula.setName("Aula");
        tipoAula.setDescription("Salón de Clases");
        spaceTypeRepository.save(tipoAula);

        Space aula = new Space();
        aula.setDescription("Aula 1");
        aula.setStatus(ReservableStatus.AVAILABLE);
        aula.setStudentsAvailable(true);
        aula.setBuilding(building);
        aula.setType(tipoAula);
        spaceRepository.save(aula);

        List<Space> spaces = spaceRepository.findByBuildingId(building.getId());

        assertThat(spaces).hasSize(1);
        assertThat(spaces.get(0).getDescription()).isEqualTo("Aula 1");
    }

    @Test
    void polymorphism_shouldFindSpaceUsingReservableRepo() {

        Building building = new Building();
        building.setName("Docencia 4");
        buildingRepository.save(building);

        SpaceType tipoLab = new SpaceType();
        tipoLab.setName("Compu Aula");
        tipoLab.setDescription("Laboratorio de Computo");
        tipoLab = spaceTypeRepository.save(tipoLab);

        Space lab = new Space();
        lab.setDescription("CA1");
        lab.setStatus(ReservableStatus.MAINTENANCE);
        lab.setStudentsAvailable(false);

        lab.setBuilding(building);
        lab.setType(tipoLab);

        spaceRepository.save(lab);

        List<Reservable> result = reservableRepository.findAll();

        assertThat(result).extracting(Reservable::getStatus)
                .contains(ReservableStatus.MAINTENANCE);
    }

}
