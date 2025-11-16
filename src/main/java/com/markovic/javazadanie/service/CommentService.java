package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.Comment;
import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.CommentRepository;
import com.markovic.javazadanie.repository.TaskRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;   // 👈 pridali sme

    public Comment create(Long taskId, Long authorId, String content) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorId));

        Comment c = Comment.builder()
                .task(task)
                .author(author)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(c);

        // 🔔 LOG: vytvorený komentár
        activityLogService.log(
                authorId,
                ActivityAction.COMMENT_CREATED,
                "Comment #" + saved.getCommentId() +
                        " on task #" + taskId +
                        " by user #" + authorId
        );

        return saved;
    }

    public List<Comment> getByTask(Long taskId) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        return commentRepository.findByTask(t);
    }

    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    public Optional<Comment> getById(Long id) {
        return commentRepository.findById(id);
    }

    public boolean delete(Long id) {
        return commentRepository.findById(id).map(c -> {
            commentRepository.delete(c);

            activityLogService.log(
                    c.getAuthor().getId(),
                    ActivityAction.COMMENT_DELETED,
                    "Comment #" + c.getCommentId() + " deleted"
            );

            return true;
        }).orElse(false);
    }
}
