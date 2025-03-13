package com.utcn.userservice.controller;

import com.utcn.userservice.entity.Study;
import com.utcn.userservice.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/studies")
public class StudyController {

    @Autowired
    private StudyService studyService;

    @GetMapping
    public ResponseEntity<List<Study>> getAllStudies() {
        return ResponseEntity.ok(studyService.retrieveStudies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Study> getStudyById(@PathVariable Integer id) {
        Study study = studyService.retrieveStudyById(id);
        if (study != null) {
            return ResponseEntity.ok(study);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Study>> getStudiesByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(studyService.retrieveStudiesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Study> createStudy(@RequestBody Study study) {
        return ResponseEntity.ok(studyService.addStudy(study));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Study> updateStudy(@PathVariable Integer id, @RequestBody Study study) {
        Study existingStudy = studyService.retrieveStudyById(id);
        if (existingStudy == null) {
            return ResponseEntity.notFound().build();
        }
        study.setId(id);
        return ResponseEntity.ok(studyService.updateStudy(study));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudy(@PathVariable Integer id) {
        Study existingStudy = studyService.retrieveStudyById(id);
        if (existingStudy == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studyService.deleteStudyById(id));
    }
} 