package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroup(StudyGroup group);

    Long taskId(Long taskId);
    long countByGroup_GroupId(Long groupId);
    long countByGroup_GroupIdAndStatus(Long groupId, TaskStatus status);

}

