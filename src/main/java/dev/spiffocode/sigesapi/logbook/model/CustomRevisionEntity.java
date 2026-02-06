package dev.spiffocode.sigesapi.logbook.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionMapping;

@Entity
@RevisionEntity
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(CustomRevisionListener.class)
public class CustomRevisionEntity extends RevisionMapping {
    private String remoteHost;
    private String remoteUser;
}