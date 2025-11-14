package com.markovic.javazadanie.repository;

import com.markovic.javazadanie.model.Resource;
import com.markovic.javazadanie.model.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ResourceRepository extends JpaRepository<Resource,Long> {
    List<Resource> findByGroup(StudyGroup group);
}
