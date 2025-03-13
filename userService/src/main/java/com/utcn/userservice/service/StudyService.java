package com.utcn.userservice.service;

import com.utcn.userservice.entity.Study;
import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudyService {

    @Autowired
    private StudyRepository studyRepository;

    public List<Study> retrieveStudies() {
        List<Study> studies = new ArrayList<>();
        studyRepository.findAll().forEach(studies::add);
        return studies;
    }

    public Study retrieveStudyById(Integer id) {
        Optional<Study> study = studyRepository.findById(id);
        return study.orElse(null);
    }
    
    public List<Study> retrieveStudiesByUser(User user) {
        return studyRepository.findByUser(user);
    }
    
    public List<Study> retrieveStudiesByUserId(Integer userId) {
        return studyRepository.findByUserId(userId);
    }

    public Study addStudy(Study study) {
        return studyRepository.save(study);
    }

    public Study updateStudy(Study study) {
        return studyRepository.save(study);
    }

    public String deleteStudyById(Integer id) {
        studyRepository.deleteById(id);
        return "Study record deleted successfully";
    }
} 