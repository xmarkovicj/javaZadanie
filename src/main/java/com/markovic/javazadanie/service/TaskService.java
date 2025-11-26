package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import com.markovic.javazadanie.repository.TaskRepository;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.websocket.TaskWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.markovic.javazadanie.model.TaskStatus;
import com.markovic.javazadanie.dto.TaskUpdateDto;

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
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskWebSocketHandler taskWebSocketHandler;

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
                .status( TaskStatus.OPEN)
                .deadline(deadline)
                .createdAt(LocalDateTime.now())
                .build();

        Task saved = taskRepository.save(t);
        taskWebSocketHandler.broadcast("NEW_TASK_CREATED: "+ t.getTitle());

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

    // v TaskService

    public Optional<Task> update(Long taskId, TaskUpdateDto dto) {
        return taskRepository.findById(taskId).map(t -> {
            if (dto.getTitle() != null) {
                t.setTitle(dto.getTitle());
            }
            if (dto.getDescription() != null) {
                t.setDescription(dto.getDescription());
            }
            if (dto.getDeadline() != null) {
                t.setDeadline(dto.getDeadline());
            }
            if (dto.getStatus() != null) {
                t.setStatus(TaskStatus.valueOf(dto.getStatus()));
            }

            return taskRepository.save(t);
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
            taskWebSocketHandler.broadcast("TASK_DELETED: " + t.getTaskId());
            return true;
        }).orElse(false);
    }

    public void closeExpiredTasks() {
        List<Task> tasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Task t : tasks) {
            if (t.getDeadline() != null
                    && t.getDeadline().isBefore(now)
                    && t.getStatus() != TaskStatus.CLOSED) {

                t.setStatus(TaskStatus.CLOSED);
                taskRepository.save(t);
            }
        }
    }


}
