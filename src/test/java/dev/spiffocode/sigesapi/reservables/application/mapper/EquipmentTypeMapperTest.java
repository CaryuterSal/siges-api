package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTestClass
public class EquipmentTypeMapperTest {

    private final EquipmentTypeMapper mapper = new dev.spiffocode.sigesapi.reservables.application.mapper.EquipmentTypeMapperImpl();

    @Test
    void register_dto_to_entity() {
        EquipmentTypeRegisterDto request = EquipmentTypeRegisterDto.builder()
                .name("Projector")
                .description("Multimedia projector")
                .build();

        EquipmentType entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
    }

    @Test
    void update_dto_to_entity() {
        EquipmentTypeUpdateDto request = new EquipmentTypeUpdateDto(
                "New Projector",
                "New Description");

        EquipmentType entity = EquipmentType.builder()
                .name("Projector")
                .description("Multimedia projector")
                .build();

        mapper.updateEntityFromDto(request, entity);

        assertEquals(request.name(), entity.getName());
        assertEquals(request.description(), entity.getDescription());
    }

    @Test
    void entity_to_dto() {
        EquipmentType entity = EquipmentType.builder()
                .id(1L)
                .name("Projector")
                .description("Multimedia projector")
                .build();

        EquipmentTypeDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getDescription(), dto.description());
    }

    @Test
    void entity_list_to_dto_list() {
        EquipmentType entity = EquipmentType.builder()
                .id(1L)
                .name("Projector")
                .description("Multimedia projector")
                .build();

        List<EquipmentTypeDto> dtos = mapper.toDto(List.of(entity));

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(entity.getId(), dtos.get(0).id());
        assertEquals(entity.getName(), dtos.get(0).name());
        assertEquals(entity.getDescription(), dtos.get(0).description());
    }
}
