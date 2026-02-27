package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "space_types")
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
@SQLDelete(sql = "UPDATE reservables SET deleted_at = NOW() WHERE id = ?")
public class SpaceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedAt;

}
