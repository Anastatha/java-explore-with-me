package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.ewmmain.model.ParticipationRequest;
import ru.practicum.explorewithme.ewmmain.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("select pr.event.id, count(pr.id) from ParticipationRequest pr where pr.event.id in :eventIds and pr.status = :status group by pr.event.id")
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);
}
