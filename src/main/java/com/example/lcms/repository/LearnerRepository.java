package com.example.lcms.repository;

import com.example.lcms.entity.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, Long> {

    /**
     * Search learners by first name, last name, or email (case-insensitive).
     */
    @Query("SELECT DISTINCT l FROM Learner l LEFT JOIN FETCH l.courses WHERE " +
           "LOWER(l.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Learner> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Fetch all learners with their courses eagerly loaded (avoids N+1).
     */
    @Query("SELECT DISTINCT l FROM Learner l LEFT JOIN FETCH l.courses")
    List<Learner> findAllWithCourses();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
