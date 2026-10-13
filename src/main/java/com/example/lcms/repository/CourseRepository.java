package com.example.lcms.repository;

import com.example.lcms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Search courses by name, code, or description (case-insensitive).
     */
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Filter courses whose date range overlaps with the given range.
     */
    @Query("SELECT c FROM Course c WHERE c.startDate >= :from AND c.endDate <= :to")
    List<Course> filterByDateRange(@Param("from") LocalDate from,
                                   @Param("to") LocalDate to);

    /**
     * Combined search + date filter.
     */
    @Query("SELECT c FROM Course c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND c.startDate >= :from AND c.endDate <= :to")
    List<Course> searchAndFilter(@Param("keyword") String keyword,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
