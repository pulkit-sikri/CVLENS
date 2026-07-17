package com.example.sra.controller;

import com.example.sra.entity.User;
import com.example.sra.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Set;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/login")
    public String adminLoginForm() {
        return "admin-login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Username is already taken");
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "Email is already registered");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userService.registerUser(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/admin/register")
    public String adminRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-register";
    }

    @PostMapping("/admin/register")
    public String registerAdmin(@Valid @ModelAttribute("user") User user,
                                BindingResult result,
                                Model model) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Username is already taken");
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "Email is already registered");
        }

        if (result.hasErrors()) {
            return "admin-register";
        }

        userService.registerAdmin(user);
        return "redirect:/admin/login?registered=true";
    }

    @GetMapping("/dashboard")
    public String roleRedirect(Authentication authentication,
                               @org.springframework.web.bind.annotation.RequestParam(value = "error", required = false) String error) {
        if (authentication == null) {
            return "redirect:/login";
        }
        String errorParam = (error != null && !error.isEmpty()) ? "?error=" + error : "";
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_AUTHOR")) {
            return "redirect:/admin/dashboard" + errorParam;
        } else if (roles.contains("ROLE_USER")) {
            return "redirect:/user/dashboard" + errorParam;
        }
        return "redirect:/login";
    }
}
