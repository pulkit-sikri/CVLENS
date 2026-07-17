package com.example.sra.controller;

import com.example.sra.entity.*;
import com.example.sra.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CompanyService companyService;
    private final JobDescriptionService jobDescriptionService;
    private final AnalysisService analysisService;

    public AdminController(UserService userService,
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
        User admin = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        long totalUsers = userService.countUsers();
        long totalCompanies = companyService.countCompanies();
        long totalJobRoles = jobDescriptionService.countJobRoles();
        long totalAnalyses = analysisService.countReports();
        Double avgScore = analysisService.getAverageScore();
        String mostApplied = analysisService.getMostAppliedCompany();
        List<AnalysisReport> recent = analysisService.getRecentReports();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalJobRoles", totalJobRoles);
        model.addAttribute("totalAnalyses", totalAnalyses);
        model.addAttribute("avgScore", avgScore);
        model.addAttribute("mostAppliedCompany", mostApplied);
        model.addAttribute("recentAnalyses", recent);
        model.addAttribute("admin", admin);

        return "admin/dashboard";
    }

    @GetMapping("/companies")
    public String viewCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("newCompany", new Company());
        return "admin/companies";
    }

    @PostMapping("/companies/add")
    public String addCompany(@ModelAttribute("newCompany") Company company) {
        if (company.getName() != null && !company.getName().trim().isEmpty()) {
            companyService.saveCompany(company);
        }
        return "redirect:/admin/companies";
    }

    @GetMapping("/companies/delete/{id}")
    public String deleteCompany(@PathVariable("id") Long id) {
        companyService.deleteCompany(id);
        return "redirect:/admin/companies";
    }

    @GetMapping("/companies/{companyId}/roles")
    public String viewCompanyRoles(@PathVariable("companyId") Long companyId, Model model) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        
        List<JobDescription> roles = jobDescriptionService.getJobDescriptionsByCompanyId(companyId);
        
        model.addAttribute("company", company);
        model.addAttribute("roles", roles);
        model.addAttribute("newRole", new JobDescription());
        return "admin/roles";
    }

    @PostMapping("/companies/{companyId}/roles/add")
    public String addCompanyRole(@PathVariable("companyId") Long companyId,
                                 @ModelAttribute("newRole") JobDescription jobDescription,
                                 @RequestParam("skillsList") String skillsList) {
        Company company = companyService.getCompanyById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        
        jobDescription.setCompany(company);
        jobDescriptionService.saveJobDescription(jobDescription, skillsList);
        
        return "redirect:/admin/companies/" + companyId + "/roles";
    }

    @GetMapping("/companies/{companyId}/roles/delete/{roleId}")
    public String deleteCompanyRole(@PathVariable("companyId") Long companyId,
                                    @PathVariable("roleId") Long roleId) {
        jobDescriptionService.deleteJobDescription(roleId);
        return "redirect:/admin/companies/" + companyId + "/roles";
    }

    @GetMapping("/users")
    public String viewUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/resumes")
    public String viewResumes(Model model) {
        List<AnalysisReport> reports = analysisService.getAllReports();
        model.addAttribute("reports", reports);
        return "admin/resumes";
    }
}
