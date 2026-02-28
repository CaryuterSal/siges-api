package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTestClass
public class SpaceTypeMapperTest {

    private final SpaceTypeMapper mapper = new SpaceTypeMapperImpl();

    @Test
    void register_dto_to_entity() {
        SpaceTypeRegisterDto request = SpaceTypeRegisterDto.builder()
                .name("Computer Lab")
                .description("Lab full of computers")
                .build();

        SpaceType entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getDescription(), entity.getDescription());
    }

    @Test
    void update_dto_to_entity() {
        SpaceTypeUpdateDto request = new SpaceTypeUpdateDto(
                "New Lab",
                "New Description");

        SpaceType entity = SpaceType.builder()
                .name("Computer Lab")
                .description("Lab full of computers")
                .build();

        mapper.updateEntityFromDto(request, entity);

        assertEquals(request.name(), entity.getName());
        assertEquals(request.description(), entity.getDescription());
    }

    @Test
    void entity_to_dto() {
        SpaceType entity = SpaceType.builder()
                .id(1L)
                .name("Computer Lab")
                .description("Lab full of computers")
                .build();

        SpaceTypeDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getDescription(), dto.description());
    }

    @Test
    void entity_list_to_dto_list() {
        SpaceType entity = SpaceType.builder()
                .id(1L)
                .name("Computer Lab")
                .description("Lab full of computers")
                .build();

        List<SpaceTypeDto> dtos = mapper.toDto(List.of(entity));

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(entity.getId(), dtos.get(0).id());
        assertEquals(entity.getName(), dtos.get(0).name());
        assertEquals(entity.getDescription(), dtos.get(0).description());
    }
}
