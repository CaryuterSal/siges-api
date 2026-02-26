package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTestClass
public class BuildingMapperTest {

    static BuildingMapper mapper = new BuildingMapperImpl();

    @Test
    void register_dto_to_entity(){
        BuildingRegisterDto request = BuildingRegisterDto.builder()
                .name("Docencia 1")
                .build();

        Building entity =  mapper.toEntity(request);
        assertEquals(entity.getName(), request.name());
    }

    @Test
    void entity_to_dto() {
        Building entity = Building.builder()
                .name("Docencia 1")
                .build();
        BuildingDto dto = mapper.toDto(entity);

        assertEquals(entity.getName(), dto.name());
    }

    @Test
    void update_dto_to_entity() {
        BuildingUpdateDto request = BuildingUpdateDto.builder()
                .name("New Docencia 1")
                .build();
        Building entity = Building.builder()
                .name("Docencia 1")
                .build();

        Building updatedEntity = mapper.updateEntityFromDto(request, entity);

        assertEquals(updatedEntity.getName(), request.name());
    }
}
