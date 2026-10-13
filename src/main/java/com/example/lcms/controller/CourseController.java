package com.example.lcms.controller;

import com.example.lcms.entity.Course;
import com.example.lcms.service.CourseService;
import com.example.lcms.service.ImageStorageService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final ImageStorageService imageStorageService;

    public CourseController(CourseService courseService, ImageStorageService imageStorageService) {
        this.courseService = courseService;
        this.imageStorageService = imageStorageService;
    }

    // ─── Page 1: Course List ─────────────────────────

    @GetMapping
    public String listCourses(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        List<Course> courses = courseService.searchAndFilter(keyword, fromDate, toDate);

        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "course/list";
    }

    // ─── Page 2: Add Course Form ─────────────────────

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("pageTitle", "Add Course");
        return "course/form";
    }

    // ─── Page 2: Edit Course Form ────────────────────

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return courseService.findById(id).map(course -> {
            model.addAttribute("course", course);
            model.addAttribute("pageTitle", "Edit Course");
            return "course/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        });
    }

    // ─── Save (create or update) ─────────────────────

    @PostMapping("/save")
    public String saveCourse(
            @Valid @ModelAttribute("course") Course course,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes ra) {

        // Check unique name
        if (courseService.isNameTaken(course.getName(), course.getId())) {
            result.rejectValue("name", "duplicate", "A course with this name already exists.");
        }

        // Check unique code
        if (courseService.isCodeTaken(course.getCode(), course.getId())) {
            result.rejectValue("code", "duplicate", "A course with this code already exists.");
        }

        // Validate end date >= start date
        if (course.getStartDate() != null && course.getEndDate() != null
                && course.getEndDate().isBefore(course.getStartDate())) {
            result.rejectValue("endDate", "invalid", "End date must be on or after the start date.");
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", course.getId() == null ? "Add Course" : "Edit Course");
            return "course/form";
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if updating
            if (course.getId() != null) {
                courseService.findById(course.getId())
                        .ifPresent(existing -> imageStorageService.delete(existing.getImagePath()));
            }
            String imagePath = imageStorageService.store(imageFile, "courses");
            course.setImagePath(imagePath);
        } else if (course.getId() != null) {
            // Preserve existing image when no new file is uploaded
            courseService.findById(course.getId())
                    .ifPresent(existing -> course.setImagePath(existing.getImagePath()));
        }

        courseService.save(course);
        ra.addFlashAttribute("successMessage", "Course saved successfully!");
        return "redirect:/courses";
    }

    // ─── Delete ──────────────────────────────────────

    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
        courseService.findById(id).ifPresent(course -> {
            imageStorageService.delete(course.getImagePath());
            courseService.deleteById(id);
        });
        ra.addFlashAttribute("successMessage", "Course deleted successfully!");
        return "redirect:/courses";
    }
}
