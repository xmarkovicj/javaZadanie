package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.Membership;
import com.markovic.javazadanie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership,Long>{
    List<Membership> findByGroup(StudyGroup group);
    List<Membership> findByUser(User user);
    boolean existsByUserAndGroup(User user, StudyGroup group);
}
