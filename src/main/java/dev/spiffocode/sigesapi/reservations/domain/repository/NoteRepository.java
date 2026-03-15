package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservations.domain.model.Note;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<@NonNull Note, @NonNull Long> {
    Optional<Note> findByIdAndReservationId(@NonNull Long noteId, @NonNull Long reservationId);
}
