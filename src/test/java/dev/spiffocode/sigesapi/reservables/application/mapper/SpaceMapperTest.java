package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTestClass
public class SpaceMapperTest {

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private SpaceTypeMapper spaceTypeMapper;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private SpaceMapperImpl mapper;

    @Test
    void to_dto() {
        Space entity = Space.builder()
                .id(1L)
                .name("Classroom A")
                .description("Big classroom")
                .bookInAdvance(Duration.ofHours(2))
                .studentsAvailable(true)
                .capacity(30)
                .type(SpaceType.builder().id(10L).build())
                .building(Building.builder().id(20L).build())
                .build();

        BuildingDto mockBuildingDto = BuildingDto.builder().id(20L).name("Building name").build();
        SpaceTypeDto mockSpaceTypeDto = SpaceTypeDto.builder().id(10L).name("Type name").build();

        when(buildingMapper.toDto(any(Building.class))).thenReturn(mockBuildingDto);
        when(spaceTypeMapper.toDto(any(SpaceType.class))).thenReturn(mockSpaceTypeDto);

        SpaceDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getBookInAdvance(), dto.getBookInAdvanceDuration());
        assertEquals(entity.isStudentsAvailable(), dto.isAvailableForStudents());
        assertEquals(mockBuildingDto.id(), dto.getBuilding().id());
        assertEquals(mockSpaceTypeDto.id(), dto.getSpaceType().id());
    }

    @Test
    void register_dto_to_entity() {
        SpaceRegisterDto request = SpaceRegisterDto.builder()
                .name("Classroom B")
                .description("Small classroom")
                .bookInAdvanceDuration(Duration.ofMinutes(30))
                .capacity(15)
                .studentsAvailable(true)
                .build();

        Building building = Building.builder().id(1L).build();
        SpaceType spaceType = SpaceType.builder().id(2L).build();

        Space entity = mapper.toEntity(request, spaceType, building);

        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getBookInAdvanceDuration(), entity.getBookInAdvance());
        assertEquals(request.getCapacity(), entity.getCapacity());
        assertEquals(request.getStudentsAvailable(), entity.isStudentsAvailable());
        assertEquals(building, entity.getBuilding());
        assertEquals(spaceType, entity.getType());
    }

    @Test
    void update_dto_to_entity() {
        SpaceUpdateDto request = SpaceUpdateDto.builder()
                .name("Classroom C")
                .description("Updated description")
                .bookInAdvanceDuration(Duration.ofHours(1))
                .capacity(20)
                .studentsAvailable(false)
                .build();

        Building building = Building.builder().id(3L).build();
        SpaceType spaceType = SpaceType.builder().id(4L).build();
        Space entity = Space.builder().id(1L).build();

        mapper.updateEntityFromDto(request, spaceType, building, entity);

        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getBookInAdvanceDuration(), entity.getBookInAdvance());
        assertEquals(request.getCapacity(), entity.getCapacity());
        assertEquals(request.getStudentsAvailable(), entity.isStudentsAvailable());
        assertEquals(building, entity.getBuilding());
        assertEquals(spaceType, entity.getType());
    }
}
