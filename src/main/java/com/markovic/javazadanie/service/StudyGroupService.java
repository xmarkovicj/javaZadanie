package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository groupRepository;

    public StudyGroup create(StudyGroup group){
        return groupRepository.save(group);
    }

    public List<StudyGroup> getAll(){
        return groupRepository.findAll();
    }

    public Optional<StudyGroup> getById(Long id){
        return groupRepository.findById(id);
    }

    public Optional<StudyGroup> update(Long id, StudyGroup data){
        return groupRepository.findById(id).map(g -> {
            g.setName(data.getName());
            g.setDescription(data.getDescription());
            return groupRepository.save(g);
        });
    }

    public boolean delete(Long id){
        if(groupRepository.existsById(id)){
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
