package com.example.lcms.service;

import com.example.lcms.entity.Course;
import com.example.lcms.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    /**
     * Searches and/or filters courses based on keyword and date range.
     * Returns all courses when no criteria are provided.
     */
    public List<Course> searchAndFilter(String keyword, LocalDate from, LocalDate to) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasFrom = from != null;
        boolean hasTo = to != null;

        if (hasKeyword && hasFrom && hasTo) {
            return courseRepository.searchAndFilter(keyword.trim(), from, to);
        } else if (hasKeyword) {
            return courseRepository.searchByKeyword(keyword.trim());
        } else if (hasFrom && hasTo) {
            return courseRepository.filterByDateRange(from, to);
        } else {
            return courseRepository.findAll();
        }
    }

    // ── Uniqueness helpers ───────────────────────────

    public boolean isNameTaken(String name, Long excludeId) {
        if (excludeId == null) {
            return courseRepository.existsByNameIgnoreCase(name);
        }
        return courseRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
    }

    public boolean isCodeTaken(String code, Long excludeId) {
        if (excludeId == null) {
            return courseRepository.existsByCodeIgnoreCase(code);
        }
        return courseRepository.existsByCodeIgnoreCaseAndIdNot(code, excludeId);
    }
}
