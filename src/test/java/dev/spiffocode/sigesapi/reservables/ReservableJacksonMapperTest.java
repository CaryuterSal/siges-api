package dev.spiffocode.sigesapi.reservables;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

@UnitTestClass
@SpringBootTest(classes = {ObjectMapper.class})
public class ReservableJacksonMapperTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize_equipment_update_dto(){
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
}
