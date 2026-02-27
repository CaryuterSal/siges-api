package dev.spiffocode.sigesapi.users.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE app_users SET deleted_at = NOW() WHERE id = ?")
@Table(name = "user_admins")
public class Admin extends User {
}
