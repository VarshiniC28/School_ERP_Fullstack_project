package com.example.schoolERP.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.schoolERP.project.dto.UserDTO;
import com.example.schoolERP.project.service.UserService;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    // ── Public landing page ───────────────────────────────────────────────────
    @GetMapping({"", "/"})
    public String home() {
        return "home";
    }

    // ── Login page ────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ── Registration page ─────────────────────────────────────────────────────
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute UserDTO userDTO,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        try {
            userService.StoreRegisteredUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Registered successfully! Please log in.");
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userDTO", userDTO);
            return "register";
        }
        return "redirect:/login";
    }

    // NOTE: All dashboard and management routes have been moved to their
    // respective controllers:
    //   /admin/**    → AdminController
    //   /faculty/**  → FacultyController
    //   /student/**  → StudentController
}