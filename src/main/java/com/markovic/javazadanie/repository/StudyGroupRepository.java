package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup,Long> {

}
