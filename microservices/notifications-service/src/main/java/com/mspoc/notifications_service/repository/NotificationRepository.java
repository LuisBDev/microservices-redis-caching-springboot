package com.mspoc.notifications_service.repository;

import com.mspoc.notifications_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Luis Balarezo
 **/
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
}
