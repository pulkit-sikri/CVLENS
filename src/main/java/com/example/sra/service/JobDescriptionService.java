package com.example.sra.service;

import com.example.sra.entity.JobDescription;
import com.example.sra.entity.Skill;
import com.example.sra.repository.JobDescriptionRepository;
import com.example.sra.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final SkillRepository skillRepository;

    public JobDescriptionService(JobDescriptionRepository jobDescriptionRepository, SkillRepository skillRepository) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.skillRepository = skillRepository;
    }

    public List<JobDescription> getAllJobDescriptions() {
        return jobDescriptionRepository.findAll();
    }

    public Optional<JobDescription> getJobDescriptionById(Long id) {
        return jobDescriptionRepository.findById(id);
    }

    public List<JobDescription> getJobDescriptionsByCompanyId(Long companyId) {
        return jobDescriptionRepository.findByCompanyId(companyId);
    }

    @Transactional
    public JobDescription saveJobDescription(JobDescription jobDescription, String skillsString) {
        // Save the JobDescription first to ensure it has an ID
        JobDescription savedJd = jobDescriptionRepository.save(jobDescription);

        // Delete existing skills for this job description if editing
        List<Skill> existingSkills = skillRepository.findByJobDescriptionId(savedJd.getId());
        skillRepository.deleteAll(existingSkills);

        // Save new skills
        List<Skill> newSkills = new ArrayList<>();
        if (skillsString != null && !skillsString.trim().isEmpty()) {
            String[] skillNames = skillsString.split(",");
            for (String name : skillNames) {
                String trimmedName = name.trim();
                if (!trimmedName.isEmpty()) {
                    newSkills.add(new Skill(trimmedName, savedJd));
                }
            }
            skillRepository.saveAll(newSkills);
        }
        
        savedJd.setSkills(newSkills);
        return savedJd;
    }

    @Transactional
    public void deleteJobDescription(Long id) {
        jobDescriptionRepository.deleteById(id);
    }

    public long countJobRoles() {
        return jobDescriptionRepository.count();
    }
}
