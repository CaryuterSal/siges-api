package dev.spiffocode.sigesapi.reservables.domain.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "inventory_items")
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
public class InventoryItem {

    @Id
    @NotBlank
    private String inventoryNum;
}
