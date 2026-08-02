package com.trendythread.app.repositories;

import com.trendythread.app.entities.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    long countByRecipientEmailAndReadFalse(String email);

    Optional<Notification> findByIdAndRecipientEmail(Integer id, String email);

    @Transactional
    @Modifying
    @Query("update Notification n set n.read = true where n.recipient.email = :email and n.read = false")
    void markAllAsReadForEmail(@Param("email") String email);
}
