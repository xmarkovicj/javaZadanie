package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.ActivityLog;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
    List<ActivityLog> findByUser(User user);
}
