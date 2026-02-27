package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@SQLDelete(sql = "UPDATE reservables SET deleted_at = NOW() WHERE id = ?")
@Table(name = "equipments")
@PrimaryKeyJoinColumn(name = "id")
public class Equipment extends Reservable {

    @ManyToOne
    @JoinColumn(name = "spaces_id")
    private Space space;

    @NotBlank
    @Column(unique = true)
    private String inventoryNum;
}
