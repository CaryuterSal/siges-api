package dev.spiffocode.sigesapi.reservables;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

@UnitTestClass
@SpringBootTest(classes = { ObjectMapper.class })
public class ReservableJacksonMapperTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize_equipment_update_dto() throws JsonProcessingException {
        EquipmentUpdateDto dto = EquipmentUpdateDto.builder()
                .inventoryNum("INV-5000")
                .name("New Laptop")
                .description("Updated!")
                .studentsAvailable(false)
                .buildingId(1L)
                .build();
        String json = objectMapper.writeValueAsString(dto);

        EquipmentUpdateDto result = objectMapper.readValue(json, EquipmentUpdateDto.class);
    }

    @Test
    void serialize_equipment_register_dto() throws JsonProcessingException {
        EquipmentRegisterDto dto = EquipmentRegisterDto.builder()
                .inventoryNum("INV-5000")
                .name("New Laptop")
                .description("Updated!")
                .studentsAvailable(false)
                .buildingId(1L)
                .build();
        String json = objectMapper.writeValueAsString(dto);

        EquipmentRegisterDto result = objectMapper.readValue(json, EquipmentRegisterDto.class);
    }


    @Test
    void serialize_space_register_dto() throws JsonProcessingException {
        SpaceRegisterDto dto = SpaceRegisterDto.builder()
                .name("New Laptop")
                .description("Updated!")
                .studentsAvailable(false)
                .buildingId(1L)
                .spaceTypeId(1L)
                .build();
        String json = objectMapper.writeValueAsString(dto);

        SpaceRegisterDto result = objectMapper.readValue(json, SpaceRegisterDto.class);
    }

    @Test
    void serialize_building_dto() throws Exception {
        BuildingDto dto = BuildingDto
                .builder()
                .id(1L)
                .name("Building A")
                .build();
        String json = objectMapper.writeValueAsString(dto);

        BuildingDto result = objectMapper.readValue(json, BuildingDto.class);
    }
}
