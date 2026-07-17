package com.example.sra.service;

import com.example.sra.entity.*;
import com.example.sra.repository.AnalysisReportRepository;
import com.example.sra.repository.CompanyRepository;
import com.example.sra.repository.JobDescriptionRepository;
import com.example.sra.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final CompanyRepository companyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    public AnalysisService(ResumeRepository resumeRepository,
                           AnalysisReportRepository analysisReportRepository,
                           CompanyRepository companyRepository,
                           JobDescriptionRepository jobDescriptionRepository) {
        this.resumeRepository = resumeRepository;
        this.analysisReportRepository = analysisReportRepository;
        this.companyRepository = companyRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    @Transactional
    public AnalysisReport analyzeResume(User user, Long companyId, Long jobDescriptionId, String resumeContent) {
        // 1. Convert resume to lowercase
        String lowercaseResume = resumeContent.toLowerCase();

        // 2. Remove punctuation
        String cleanedResume = lowercaseResume.replaceAll("[^a-zA-Z0-9\\s]", " ");

        // 3. Split into words
        String[] words = cleanedResume.split("\\s+");

        // 4. Store words in a HashSet for O(1) lookups
        Set<String> resumeWords = new HashSet<>();
        for (String word : words) {
            String trimmedWord = word.trim();
            if (!trimmedWord.isEmpty()) {
                resumeWords.add(trimmedWord);
            }
        }

        // 5. Load company and job details
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Job Description not found"));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        
        List<Skill> requiredSkills = jd.getSkills();

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        // 6. Check each required skill using simple contains/HashSet lookup
        for (Skill skill : requiredSkills) {
            String skillName = skill.getName().trim().toLowerCase();
            
            boolean isMatched = false;
            if (skillName.contains(" ")) {
                // Multi-word skill: check if the cleaned resume string contains the exact phrase
                isMatched = cleanedResume.contains(skillName);
            } else {
                // Single-word skill: check direct presence in HashSet
                isMatched = resumeWords.contains(skillName);
            }

            if (isMatched) {
                matched.add(skill.getName());
            } else {
                missing.add(skill.getName());
            }
        }

        // 7. Calculate score
        int totalSkills = requiredSkills.size();
        int score = 0;
        if (totalSkills > 0) {
            score = (int) Math.round(((double) matched.size() / totalSkills) * 100);
        } else {
            score = 100;
        }

        // 8. Assign status
        String status;
        if (score >= 90) {
            status = "Excellent";
        } else if (score >= 75) {
            status = "Good";
        } else if (score >= 50) {
            status = "Average";
        } else {
            status = "Needs Improvement";
        }

        // 9. Generate suggestions
        StringBuilder suggestions = new StringBuilder();
        if (missing.isEmpty()) {
            suggestions.append("Your resume matches all required skills for this role! Excellent job.");
        } else {
            suggestions.append("Your resume is missing the following skills required for this job role: ")
                    .append(String.join(", ", missing))
                    .append(".\n\nSuggestions:\n");
            for (String s : missing) {
                suggestions.append("- Consider learning and gaining hands-on experience in '").append(s).append("' to list on your resume.\n");
            }
        }

        // 10. Save Resume
        Resume resume = new Resume(user, resumeContent, LocalDateTime.now());
        resume = resumeRepository.save(resume);

        // 11. Save Analysis Report
        String matchedSkillsStr = String.join(", ", matched);
        String missingSkillsStr = String.join(", ", missing);
        
        AnalysisReport report = new AnalysisReport(
                resume,
                company,
                jd,
                score,
                matchedSkillsStr,
                missingSkillsStr,
                suggestions.toString(),
                status,
                LocalDateTime.now()
        );
        
        return analysisReportRepository.save(report);
    }

    public List<AnalysisReport> getReportsForUser(Long userId) {
        return analysisReportRepository.findByResumeUserIdOrderByAnalyzedAtDesc(userId);
    }

    public List<AnalysisReport> getRecentReportsForUser(Long userId) {
        return analysisReportRepository.findFirst5ByResumeUserIdOrderByAnalyzedAtDesc(userId);
    }

    public List<AnalysisReport> getRecentReports() {
        return analysisReportRepository.findFirst5ByOrderByAnalyzedAtDesc();
    }

    public Optional<AnalysisReport> getReportById(Long id) {
        return analysisReportRepository.findById(id);
    }

    public List<AnalysisReport> getAllReports() {
        return analysisReportRepository.findAll();
    }

    public long countReports() {
        return analysisReportRepository.count();
    }

    public Double getAverageScore() {
        Double avg = analysisReportRepository.getAverageScore();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public Double getAverageScoreForUser(Long userId) {
        Double avg = analysisReportRepository.getAverageScoreByUserId(userId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public Integer getHighestScoreForUser(Long userId) {
        Integer max = analysisReportRepository.getHighestScoreByUserId(userId);
        return max != null ? max : 0;
    }

    public String getMostAppliedCompany() {
        List<AnalysisReport> reports = analysisReportRepository.findAll();
        if (reports.isEmpty()) {
            return "N/A";
        }
        Map<String, Long> frequencies = reports.stream()
                .collect(Collectors.groupingBy(r -> r.getCompany().getName(), Collectors.counting()));
        
        return frequencies.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
}
