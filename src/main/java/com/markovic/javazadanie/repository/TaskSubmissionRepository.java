package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.TaskSubmission;
import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByTask(Task task);

    List<TaskSubmission> findByUser(User user);
    // v TaskSubmissionRepository.java
    boolean existsByTask_TaskIdAndUser_Id(Long taskId, Long userId);

    Optional<TaskSubmission> findByTask_TaskIdAndUser_Id(Long taskId, Long userId);

}
