package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.TaskSubmission;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.TaskRepository;
import com.markovic.javazadanie.repository.TaskSubmissionRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public TaskSubmission create(Long taskId, Long userId,
                                 String content, String attachmentUrl) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (submissionRepository.existsByTask_TaskIdAndUser_Id(taskId, userId)) {
            throw new RuntimeException("User already submitted this task");
        }

        TaskSubmission sub = TaskSubmission.builder()
                .task(task)
                .user(user)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .submittedAt(LocalDateTime.now())
                .build();

        TaskSubmission saved = submissionRepository.save(sub);

        // 🔔 LOG: odovzdanie úlohy
        activityLogService.log(
                userId,
                ActivityAction.SUBMISSION_CREATED,
                "Submission #" + saved.getSubmissionId() +
                        " for task #" + taskId +
                        " by user #" + userId
        );

        return saved;
    }

    public List<TaskSubmission> getAll() {
        return submissionRepository.findAll();
    }
    public Optional<TaskSubmission> findByTaskAndUser(Long taskId, Long userId) {
        return submissionRepository.findByTask_TaskIdAndUser_Id(taskId, userId);
    }



    public Optional<TaskSubmission> getById(Long id) {
        return submissionRepository.findById(id);
    }

    public List<TaskSubmission> getByTask(Long taskId) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        return submissionRepository.findByTask(t);
    }

    public List<TaskSubmission> getByUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return submissionRepository.findByUser(u);
    }

    // update známky a feedbacku
    public Optional<TaskSubmission> grade(Long submissionId, Double grade, String feedback) {
        return submissionRepository.findById(submissionId).map(s -> {
            s.setGrade(grade);
            s.setFeedback(feedback);
            TaskSubmission updated = submissionRepository.save(s);

            // 🔔 LOG: hodnotenie submissionu
            Long actorId = s.getUser().getId(); // ak chceš neskôr pridať graderId, vieš to rozšíriť
            activityLogService.log(
                    actorId,
                    ActivityAction.SUBMISSION_GRADED,
                    "Submission #" + updated.getSubmissionId() +
                            " graded with " + grade
            );

            return updated;
        });
    }

    public boolean delete(Long id) {
        return submissionRepository.findById(id).map(s -> {
            submissionRepository.delete(s);

            activityLogService.log(
                    s.getUser().getId(),
                    ActivityAction.SUBMISSION_DELETED,
                    "Submission #" + s.getSubmissionId() + " deleted"
            );

            return true;
        }).orElse(false);
    }
}
