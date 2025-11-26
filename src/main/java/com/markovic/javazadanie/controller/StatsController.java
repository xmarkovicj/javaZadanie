package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.dto.GroupStatsDto;
import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.TaskStatus;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.repository.ActivityLogRepository;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import com.markovic.javazadanie.repository.TaskRepository;
import com.markovic.javazadanie.repository.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StudyGroupRepository groupRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupStatsDto> getGroupStats(@PathVariable Long groupId) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        GroupStatsDto dto = new GroupStatsDto();
        dto.setGroupId(group.getGroupId());
        dto.setGroupName(group.getName());

        long total = taskRepository.countByGroup_GroupId(groupId);
        long open = taskRepository.countByGroup_GroupIdAndStatus(groupId, TaskStatus.OPEN);
        long submitted = taskRepository.countByGroup_GroupIdAndStatus(groupId, TaskStatus.SUBMITTED);
        long closed = taskRepository.countByGroup_GroupIdAndStatus(groupId, TaskStatus.CLOSED);

        dto.setTotalTasks(total);
        dto.setOpenTasks(open);
        dto.setSubmittedTasks(submitted);
        dto.setClosedTasks(closed);

        double completion = total == 0 ? 0.0 : (closed * 100.0 / total);
        dto.setCompletionRate(completion);

        // submissions per user v skupine
        Map<String, Long> memberSubs =
                submissionRepository.findByTask_Group_GroupId(groupId).stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getUser().getEmail(),
                                Collectors.counting()
                        ));
        dto.setMemberSubmissions(memberSubs);

        // aktivita za posledných 7 dní (napr. SUBMISSION_CREATED, TASK_CREATED)
        LocalDateTime from = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(1).atStartOfDay();

        Map<String, Long> activity = activityLogRepository
                .findByTimestampBetweenAndActionIn(
                        from,
                        to,
                        List.of(
                                ActivityAction.TASK_CREATED.name(),
                                ActivityAction.SUBMISSION_CREATED.name()
                        )
                )
                .stream()
                .collect(Collectors.groupingBy(
                        log -> log.getTimestamp().toLocalDate().toString(),
                        Collectors.counting()
                ));
        dto.setActivityPerDay(activity);

        return ResponseEntity.ok(dto);
    }
}
