package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.Membership;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.MembershipRepository;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    public Membership addUserToGroup(Long userId, Long groupId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        StudyGroup group =studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("StudyGroup not found with id: " + groupId));

        if(membershipRepository.existsByUser_IdAndGroup_Id(userId, groupId)){
            throw new RuntimeException("Membership already exists");
        }
        Membership m = Membership.builder()
                .user(user)
                .group(group)
                .role(role != null ? role :"MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(m);
    }
    public List<Membership> getMembersOfGroup(Long groupId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("StudyGroup not found with id: " + groupId));
        return membershipRepository.findByGroup(group);
    }
}
