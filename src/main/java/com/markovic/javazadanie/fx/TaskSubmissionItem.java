package com.markovic.javazadanie.fx;

public class TaskSubmissionItem {

    private Long id;
    private String userEmail;
    private String content;
    private String submittedAt;
    private String status;
    private String grade;

    public TaskSubmissionItem() {
    }

    public TaskSubmissionItem(Long id, String userEmail, String content,
                              String submittedAt, String status, String grade) {
        this.id = id;
        this.userEmail = userEmail;
        this.content = content;
        this.submittedAt = submittedAt;
        this.status = status;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
