package com.example.lcms.service;

import com.example.lcms.entity.Learner;
import com.example.lcms.repository.LearnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LearnerService {

    private final LearnerRepository learnerRepository;

    public LearnerService(LearnerRepository learnerRepository) {
        this.learnerRepository = learnerRepository;
    }

    public List<Learner> findAllWithCourses() {
        return learnerRepository.findAllWithCourses();
    }

    public Optional<Learner> findById(Long id) {
        return learnerRepository.findById(id);
    }

    public Learner save(Learner learner) {
        return learnerRepository.save(learner);
    }

    public void deleteById(Long id) {
        learnerRepository.deleteById(id);
    }

    public List<Learner> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return learnerRepository.findAllWithCourses();
        }
        return learnerRepository.searchByKeyword(keyword.trim());
    }

    public boolean isEmailTaken(String email, Long excludeId) {
        if (excludeId == null) {
            return learnerRepository.existsByEmailIgnoreCase(email);
        }
        return learnerRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
    }
}
