package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.Resource;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.ResourceRepository;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Resource create(Long groupId, Long uploadedById,
                           String title, String type, String pathOrUrl) {

        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        User uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new RuntimeException("User not found: " + uploadedById));

        Resource r = Resource.builder()
                .group(group)
                .uploadedBy(uploader)
                .title(title)
                .type(type)
                .pathOrUrl(pathOrUrl)
                .uploadedAt(LocalDateTime.now())
                .build();

        Resource saved = resourceRepository.save(r);

        activityLogService.log(
                uploadedById,
                ActivityAction.RESOURCE_UPLOADED,
                "Resource #" + saved.getResourceId() + " \"" + saved.getTitle() + "\" uploaded to group " + groupId
        );

        return saved;
    }

    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    public Optional<Resource> getById(Long id) {
        return resourceRepository.findById(id);
    }

    public List<Resource> getByGroup(Long groupId) {
        StudyGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        return resourceRepository.findByGroup(g);
    }

    public Optional<Resource> update(Long resourceId, String title, String type, String pathOrUrl) {
        return resourceRepository.findById(resourceId).map(r -> {
            if (title != null) r.setTitle(title);
            if (type != null) r.setType(type);
            if (pathOrUrl != null) r.setPathOrUrl(pathOrUrl);

            Resource updated = resourceRepository.save(r);

            activityLogService.log(
                    r.getUploadedBy().getId(),
                    ActivityAction.RESOURCE_UPDATED,
                    "Resource #" + updated.getResourceId() + " updated"
            );

            return updated;
        });
    }

    public boolean delete(Long id) {
        return resourceRepository.findById(id).map(r -> {
            resourceRepository.delete(r);
            activityLogService.log(
                    r.getUploadedBy().getId(),
                    ActivityAction.RESOURCE_DELETED,
                    "Resource #" + r.getResourceId() + " deleted"
            );
            return true;
        }).orElse(false);
    }
}
