package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.Comment;
import com.markovic.javazadanie.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
}
