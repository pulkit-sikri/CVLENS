package com.example.sra.controller;

import com.example.sra.dto.LoginDto;
import com.example.sra.dto.ResumeSubmissionDto;
import com.example.sra.entity.*;
import com.example.sra.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RestApiController {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobDescriptionService jobDescriptionService;
    private final AnalysisService analysisService;
    private final PasswordEncoder passwordEncoder;

    public RestApiController(UserService userService,
                             CompanyService companyService,
                             JobDescriptionService jobDescriptionService,
                             AnalysisService analysisService,
                             PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.companyService = companyService;
        this.jobDescriptionService = jobDescriptionService;
        this.analysisService = analysisService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. User Registration API
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username already taken"));
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email already registered"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password must be at least 6 characters"));
        }
        User registered = userService.registerUser(user);
        return ResponseEntity.ok(Map.of("success", true, "userId", registered.getId(), "message", "Registration successful"));
    }

    // 2. User Login API
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        Optional<User> userOpt = userService.findByUsername(loginDto.getUsername());
        if (userOpt.isPresent() && passwordEncoder.matches(loginDto.getPassword(), userOpt.get().getPassword())) {
            User u = userOpt.get();
            if ("ROLE_USER".equals(u.getRole())) {
                return ResponseEntity.ok(Map.of("success", true, "role", "ROLE_USER", "username", u.getUsername(), "userId", u.getId()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid user credentials"));
    }

    // 3. Admin Login API
    @PostMapping("/auth/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginDto loginDto) {
        Optional<User> userOpt = userService.findByUsername(loginDto.getUsername());
        if (userOpt.isPresent() && passwordEncoder.matches(loginDto.getPassword(), userOpt.get().getPassword())) {
            User u = userOpt.get();
            if ("ROLE_ADMIN".equals(u.getRole())) {
                return ResponseEntity.ok(Map.of("success", true, "role", "ROLE_ADMIN", "username", u.getUsername(), "userId", u.getId()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid admin credentials"));
    }

    // 4. Company CRUD APIs
    @GetMapping("/companies")
    public ResponseEntity<?> getCompanies() {
        List<Map<String, Object>> response = companyService.getAllCompanies().stream()
                .map(c -> Map.of("id", (Object) c.getId(), "name", (Object) c.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/companies")
    public ResponseEntity<?> createCompany(@RequestBody Company company) {
        if (company.getName() == null || company.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Company name is required"));
        }
        Company saved = companyService.saveCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "id", saved.getId(), "name", saved.getName()));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody Company companyDetails) {
        return companyService.getCompanyById(id).map(company -> {
            company.setName(companyDetails.getName());
            Company updated = companyService.saveCompany(company);
            return ResponseEntity.ok(Map.of("success", true, "id", updated.getId(), "name", updated.getName()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        if (companyService.getCompanyById(id).isPresent()) {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Company deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    // 5. Job Description CRUD APIs
    @GetMapping("/job-descriptions")
    public ResponseEntity<?> getJobDescriptions() {
        List<Map<String, Object>> response = jobDescriptionService.getAllJobDescriptions().stream()
                .map(j -> Map.of(
                        "id", (Object) j.getId(),
                        "jobRole", (Object) j.getJobRole(),
                        "description", (Object) j.getDescription(),
                        "companyId", (Object) j.getCompany().getId(),
                        "companyName", (Object) j.getCompany().getName()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/companies/{companyId}/roles")
    public ResponseEntity<?> getRolesByCompany(@PathVariable Long companyId) {
        List<Map<String, Object>> response = jobDescriptionService.getJobDescriptionsByCompanyId(companyId).stream()
                .map(j -> Map.of(
                        "id", (Object) j.getId(),
                        "jobRole", (Object) j.getJobRole(),
                        "description", (Object) j.getDescription()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/job-descriptions")
    public ResponseEntity<?> createJobDescription(@RequestParam("companyId") Long companyId,
                                                   @RequestParam("jobRole") String jobRole,
                                                   @RequestParam("description") String description,
                                                   @RequestParam(value = "skills", defaultValue = "") String skills) {
        return companyService.getCompanyById(companyId).map(company -> {
            JobDescription jd = new JobDescription(company, jobRole, description);
            JobDescription saved = jobDescriptionService.saveJobDescription(jd, skills);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "id", saved.getId(),
                    "jobRole", saved.getJobRole(),
                    "companyId", company.getId()
            ));
        }).orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/job-descriptions/{id}")
    public ResponseEntity<?> updateJobDescription(@PathVariable Long id,
                                                   @RequestParam("jobRole") String jobRole,
                                                   @RequestParam("description") String description,
                                                   @RequestParam(value = "skills", defaultValue = "") String skills) {
        return jobDescriptionService.getJobDescriptionById(id).map(jd -> {
            jd.setJobRole(jobRole);
            jd.setDescription(description);
            JobDescription updated = jobDescriptionService.saveJobDescription(jd, skills);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", updated.getId(),
                    "jobRole", updated.getJobRole()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/job-descriptions/{id}")
    public ResponseEntity<?> deleteJobDescription(@PathVariable Long id) {
        if (jobDescriptionService.getJobDescriptionById(id).isPresent()) {
            jobDescriptionService.deleteJobDescription(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Job role deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    // 6. Resume Submission & Analysis API
    @PostMapping("/analysis")
    public ResponseEntity<?> analyzeResume(@RequestBody ResumeSubmissionDto submissionDto) {
        Optional<User> userOpt = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(submissionDto.getUserId()))
                .findFirst();
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid user ID"));
        }
        
        try {
            AnalysisReport report = analysisService.analyzeResume(
                    userOpt.get(),
                    submissionDto.getCompanyId(),
                    submissionDto.getJobDescriptionId(),
                    submissionDto.getResumeContent()
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reportId", report.getId(),
                    "score", report.getScore(),
                    "status", report.getStatus(),
                    "matchedSkills", report.getMatchedSkills(),
                    "missingSkills", report.getMissingSkills(),
                    "suggestions", report.getSuggestions()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 7. Get Report Details API
    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getReportDetails(@PathVariable Long id) {
        return analysisService.getReportById(id).map(report -> {
            Map<String, Object> response = Map.of(
                    "id", report.getId(),
                    "candidate", report.getResume().getUser().getUsername(),
                    "company", report.getCompany().getName(),
                    "role", report.getJobDescription().getJobRole(),
                    "score", report.getScore(),
                    "status", report.getStatus(),
                    "matchedSkills", report.getMatchedSkills() != null ? report.getMatchedSkills() : "",
                    "missingSkills", report.getMissingSkills() != null ? report.getMissingSkills() : "",
                    "suggestions", report.getSuggestions(),
                    "analyzedAt", report.getAnalyzedAt().toString()
            );
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
}
