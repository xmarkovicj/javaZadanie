package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import com.markovic.javazadanie.repository.TaskRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Task create(Long groupId, Long createdByUserId, String title, String description,
                       String status, LocalDateTime deadline) {

        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + createdByUserId));

        Task t = Task.builder()
                .group(group)
                .createdBy(creator)
                .title(title)
                .description(description)
                .status(status != null ? status : "OPEN")
                .deadline(deadline)
                .createdAt(LocalDateTime.now())
                .build();

        Task saved = taskRepository.save(t);

        activityLogService.log(
                createdByUserId,
                ActivityAction.TASK_CREATED,
                "Task #" + saved.getTaskId() + " \"" + saved.getTitle() + "\" created in group " + groupId
        );

        return saved;
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> getById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getByGroup(Long groupId) {
        StudyGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        return taskRepository.findByGroup(g);
    }

    public Optional<Task> update(Long taskId, String title, String description,
                                 String status, LocalDateTime deadline) {
        return taskRepository.findById(taskId).map(t -> {
            if (title != null) t.setTitle(title);
            if (description != null) t.setDescription(description);
            if (status != null) t.setStatus(status);
            t.setDeadline(deadline);

            Task updated = taskRepository.save(t);

            activityLogService.log(
                    t.getCreatedBy().getId(),
                    ActivityAction.TASK_UPDATED,
                    "Task #" + updated.getTaskId() + " updated"
            );

            return updated;
        });
    }

    public boolean delete(Long id) {
        return taskRepository.findById(id).map(t -> {
            taskRepository.delete(t);
            activityLogService.log(
                    t.getCreatedBy().getId(),
                    ActivityAction.TASK_DELETED,
                    "Task #" + t.getTaskId() + " deleted"
            );
            return true;
        }).orElse(false);
    }
}
