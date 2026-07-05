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

    @Query("""
            select r.event.id as eventId, count(r) as count
            from ParticipationRequest r
            where r.event.id in :eventIds and r.status = :status
            group by r.event.id
            """)
    List<ParticipationRequestCount> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                                             @Param("status") RequestStatus status);

    interface ParticipationRequestCount {
        Long getEventId();

        Long getCount();
    }
}
