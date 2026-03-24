package com.linkedin.repository;

import com.linkedin.entity.Connection;
import com.linkedin.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // Bidirectional: find connection between two users regardless of who sent it
    @Query("SELECT c FROM Connection c WHERE " +
           "(c.sender.id = :userId1 AND c.receiver.id = :userId2) OR " +
           "(c.sender.id = :userId2 AND c.receiver.id = :userId1)")
    Optional<Connection> findConnectionBetween(@Param("userId1") Long userId1,
                                               @Param("userId2") Long userId2);

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.status = :status")
    List<Connection> findByUserIdAndStatus(@Param("userId") Long userId,
                                           @Param("status") ConnectionStatus status);

    @Query("SELECT CASE WHEN c.sender.id = :userId THEN c.receiver.id ELSE c.sender.id END " +
           "FROM Connection c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.status = 'ACCEPTED'")
    List<Long> findConnectionUserIds(@Param("userId") Long userId);

    // Bidirectional existence check (both directions)
    @Query("SELECT COUNT(c) > 0 FROM Connection c WHERE " +
           "((c.sender.id = :senderId AND c.receiver.id = :receiverId) OR " +
           " (c.sender.id = :receiverId AND c.receiver.id = :senderId)) " +
           "AND c.status = :status")
    boolean existsBetweenUsersWithStatus(@Param("senderId") Long senderId,
                                          @Param("receiverId") Long receiverId,
                                          @Param("status") ConnectionStatus status);

    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, ConnectionStatus status);
}
