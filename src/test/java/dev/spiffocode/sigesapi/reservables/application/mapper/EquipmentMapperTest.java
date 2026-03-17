package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTestClass
public class EquipmentMapperTest {

    @Mock
    private BuildingMapper buildingMapper;

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private EquipmentMapperImpl mapper;

    @Test
    void to_dto() {
        Equipment entity = Equipment.builder()
                .id(1L)
                .name("Projector")
                .description("HDMI Projector")
                .inventoryNum("INV-123")
                .studentsAvailable(false)
                .building(Building.builder().id(10L).build())
                .space(Space.builder().id(20L).build())
                .build();

        BuildingDto mockBuildingDto = BuildingDto.builder().id(10L).name("Building").build();
        SpaceDto mockSpaceDto = SpaceDto.builder().id(20L).name("Space").build();

        when(buildingMapper.toDto(any(Building.class))).thenReturn(mockBuildingDto);
        when(spaceMapper.toDto(any(Space.class))).thenReturn(mockSpaceDto);

        EquipmentDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getInventoryNum(), dto.getInventoryIdNum());
        assertEquals(entity.isStudentsAvailable(), dto.isAvailableForStudents());
        assertEquals(mockBuildingDto.id(), dto.getBuilding().id());
        assertEquals(mockSpaceDto.getId(), dto.getSpaceAttached().getId());
    }

    @Test
    void register_dto_to_entity() {
        EquipmentRegisterDto request = EquipmentRegisterDto.builder()
                .name("Projector")
                .description("HDMI Projector")
                .inventoryNum("INV-123")
                .studentsAvailable(false)
                .build();

        Building building = Building.builder().id(10L).build();
        Space space = Space.builder().id(20L).build();
        EquipmentType et = EquipmentType.builder().id(10L).build();

        Equipment entity = mapper.toEntity(request, building, space, et);

        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getInventoryNum(), entity.getInventoryNum());
        assertEquals(request.getStudentsAvailable(), entity.isStudentsAvailable());
        assertEquals(building, entity.getBuilding());
        assertEquals(space, entity.getSpace());
    }

    @Test
    void update_dto_to_entity() {
        EquipmentUpdateDto request = EquipmentUpdateDto.builder()
                .name("Projector Updated")
                .description("VGA Projector")
                .inventoryNum("INV-456")
                .studentsAvailable(true)
                .build();

        Building building = Building.builder().id(10L).build();
        Space space = Space.builder().id(20L).build();
        Equipment entity = Equipment.builder().id(1L).build();
        EquipmentType et = EquipmentType.builder().id(10L).build();

        mapper.updateEntityFromDto(request, building, space, et, entity);

        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getInventoryNum(), entity.getInventoryNum());
        assertEquals(request.getStudentsAvailable(), entity.isStudentsAvailable());
        assertEquals(building, entity.getBuilding());
        assertEquals(space, entity.getSpace());
    }
}
