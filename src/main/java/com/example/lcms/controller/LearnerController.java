package com.example.lcms.controller;

import com.example.lcms.entity.Course;
import com.example.lcms.entity.Learner;
import com.example.lcms.service.CourseService;
import com.example.lcms.service.ImageStorageService;
import com.example.lcms.service.LearnerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/learners")
public class LearnerController {

    private final LearnerService learnerService;
    private final CourseService courseService;
    private final ImageStorageService imageStorageService;

    public LearnerController(LearnerService learnerService,
                             CourseService courseService,
                             ImageStorageService imageStorageService) {
        this.learnerService = learnerService;
        this.courseService = courseService;
        this.imageStorageService = imageStorageService;
    }

    // ─── Page 3: Learner List ────────────────────────

    @GetMapping
    public String listLearners(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        List<Learner> learners = learnerService.searchByKeyword(keyword);

        model.addAttribute("learners", learners);
        model.addAttribute("keyword", keyword);
        return "learner/list";
    }

    // ─── Page 4: Add Learner Form ────────────────────

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("learner", new Learner());
        model.addAttribute("allCourses", courseService.findAll());
        model.addAttribute("pageTitle", "Add Learner");
        return "learner/form";
    }

    // ─── Page 4: Edit Learner Form ───────────────────

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return learnerService.findById(id).map(learner -> {
            model.addAttribute("learner", learner);
            model.addAttribute("allCourses", courseService.findAll());
            model.addAttribute("pageTitle", "Edit Learner");
            return "learner/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Learner not found.");
            return "redirect:/learners";
        });
    }

    // ─── Save (create or update) ─────────────────────

    @PostMapping("/save")
    public String saveLearner(
            @Valid @ModelAttribute("learner") Learner learner,
            BindingResult result,
            @RequestParam(value = "courseIds", required = false) List<Long> courseIds,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes ra) {

        // Check unique email
        if (learnerService.isEmailTaken(learner.getEmail(), learner.getId())) {
            result.rejectValue("email", "duplicate", "A learner with this email already exists.");
        }

        // Resolve selected courses
        Set<Course> selectedCourses = new HashSet<>();
        if (courseIds != null && !courseIds.isEmpty()) {
            for (Long courseId : courseIds) {
                courseService.findById(courseId).ifPresent(selectedCourses::add);
            }
        }
        learner.setCourses(selectedCourses);

        // Validate at least one course
        if (selectedCourses.isEmpty()) {
            result.rejectValue("courses", "NotEmpty", "Please select at least one course.");
        }

        if (result.hasErrors()) {
            model.addAttribute("allCourses", courseService.findAll());
            model.addAttribute("pageTitle", learner.getId() == null ? "Add Learner" : "Edit Learner");
            return "learner/form";
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            if (learner.getId() != null) {
                learnerService.findById(learner.getId())
                        .ifPresent(existing -> imageStorageService.delete(existing.getImagePath()));
            }
            String imagePath = imageStorageService.store(imageFile, "learners");
            learner.setImagePath(imagePath);
        } else if (learner.getId() != null) {
            learnerService.findById(learner.getId())
                    .ifPresent(existing -> learner.setImagePath(existing.getImagePath()));
        }

        learnerService.save(learner);
        ra.addFlashAttribute("successMessage", "Learner saved successfully!");
        return "redirect:/learners";
    }

    // ─── Delete ──────────────────────────────────────

    @GetMapping("/delete/{id}")
    public String deleteLearner(@PathVariable Long id, RedirectAttributes ra) {
        learnerService.findById(id).ifPresent(learner -> {
            imageStorageService.delete(learner.getImagePath());
            learnerService.deleteById(id);
        });
        ra.addFlashAttribute("successMessage", "Learner deleted successfully!");
        return "redirect:/learners";
    }
}
