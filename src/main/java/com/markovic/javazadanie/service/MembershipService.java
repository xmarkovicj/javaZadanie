package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.Membership;
import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.MembershipRepository;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;

    public Membership addMember(Long groupId, Long userId, String role) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (membershipRepository.existsByUserAndGroup(user, group)) {
            throw new RuntimeException("User is already a member of this group");
        }

        Membership m = Membership.builder()
                .group(group)
                .user(user)
                .role(role != null ? role : "MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(m);
    }

    public List<Membership> getAll() {
        return membershipRepository.findAll();
    }

    public Optional<Membership> getById(Long id) {
        return membershipRepository.findById(id);
    }

    public List<Membership> getByGroup(Long groupId) {
        StudyGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        return membershipRepository.findByGroup(g);
    }

    public List<Membership> getByUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return membershipRepository.findByUser(u);
    }

    public Optional<Membership> updateRole(Long membershipId, String role) {
        return membershipRepository.findById(membershipId).map(m -> {
            m.setRole(role);
            return membershipRepository.save(m);
        });
    }

    public boolean remove(Long membershipId) {
        if (membershipRepository.existsById(membershipId)) {
            membershipRepository.deleteById(membershipId);
            return true;
        }
        return false;
    }
    public boolean removeMember(Long groupId, Long userId) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return membershipRepository.findByUserAndGroup(user, group)
                .map(m -> {
                    membershipRepository.delete(m);
                    return true;
                })
                .orElse(false);
    }
}
