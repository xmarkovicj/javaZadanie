package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.ActivityLog;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
    List<ActivityLog> findByUser(User user);
    void deleteByUser_Id(Long userId);
    List<ActivityLog> findByTimestampBetweenAndActionIn(
            LocalDateTime from,
            LocalDateTime to,
            List<String> actions
    );

}
