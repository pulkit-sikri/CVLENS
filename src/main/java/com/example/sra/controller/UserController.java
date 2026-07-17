package com.example.sra.controller;

import com.example.sra.entity.*;
import com.example.sra.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobDescriptionService jobDescriptionService;
    private final AnalysisService analysisService;

    public UserController(UserService userService,
                          CompanyService companyService,
                          JobDescriptionService jobDescriptionService,
                          AnalysisService analysisService) {
        this.userService = userService;
        this.companyService = companyService;
        this.jobDescriptionService = jobDescriptionService;
        this.analysisService = analysisService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // User Dashboard Metrics
        List<AnalysisReport> reports = analysisService.getReportsForUser(user.getId());
        long totalAnalyses = reports.size();
        int highestScore = analysisService.getHighestScoreForUser(user.getId());
        double averageScore = analysisService.getAverageScoreForUser(user.getId());

        List<AnalysisReport> recent = analysisService.getRecentReportsForUser(user.getId());
        List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("totalAnalyses", totalAnalyses);
        model.addAttribute("highestScore", highestScore);
        model.addAttribute("averageScore", averageScore);
        model.addAttribute("recentAnalyses", recent);
        model.addAttribute("companies", companies);
        model.addAttribute("user", user);

        return "user/dashboard";
    }

    @GetMapping("/analyze")
    public String analyzeForm(Principal principal, Model model) {
        return "redirect:/user/dashboard";
    }

    @PostMapping("/analyze")
    public String runAnalysis(Principal principal,
                              @RequestParam("companyId") Long companyId,
                              @RequestParam("jobDescriptionId") Long jobDescriptionId,
                              @RequestParam("resumeText") String resumeText) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AnalysisReport report = analysisService.analyzeResume(user, companyId, jobDescriptionId, resumeText);
        return "redirect:/user/report/" + report.getId();
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") Long id, Principal principal, Model model) {
        AnalysisReport report = analysisService.getReportById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Security check: ensure user owns this report or is an Admin
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!report.getResume().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals("ROLE_ADMIN") && 
            !user.getRole().equals("ROLE_AUTHOR")) {
            return "redirect:/dashboard?error=unauthorized";
        }

        model.addAttribute("report", report);
        model.addAttribute("user", user);
        return "user/report";
    }

    @GetMapping("/report/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable("id") Long id, Principal principal) {
        AnalysisReport report = analysisService.getReportById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!report.getResume().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals("ROLE_ADMIN") && 
            !user.getRole().equals("ROLE_AUTHOR")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("           CVLens ANALYSIS REPORT          \n");
        sb.append("=========================================\n\n");
        sb.append("Candidate Username: ").append(report.getResume().getUser().getUsername()).append("\n");
        sb.append("Target Company: ").append(report.getCompany().getName()).append("\n");
        sb.append("Target Job Role: ").append(report.getJobDescription().getJobRole()).append("\n");
        sb.append("Analysis Date: ").append(report.getAnalyzedAt().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("RESUME SCORE: ").append(report.getScore()).append("% (").append(report.getStatus()).append(")\n");
        sb.append("-----------------------------------------\n\n");
        sb.append("Matched Skills:\n");
        if (report.getMatchedSkills() == null || report.getMatchedSkills().isEmpty()) {
            sb.append("  None\n");
        } else {
            for (String skill : report.getMatchedSkills().split(", ")) {
                sb.append("  [x] ").append(skill).append("\n");
            }
        }
        sb.append("\nMissing Skills:\n");
        if (report.getMissingSkills() == null || report.getMissingSkills().isEmpty()) {
            sb.append("  None\n");
        } else {
            for (String skill : report.getMissingSkills().split(", ")) {
                sb.append("  [ ] ").append(skill).append("\n");
            }
        }
        sb.append("\nSuggestions:\n").append(report.getSuggestions()).append("\n");
        sb.append("\n=========================================\n");

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "Resume_Analysis_Report_" + id + ".txt");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
