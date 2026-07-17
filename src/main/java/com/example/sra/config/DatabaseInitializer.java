package com.example.sra.config;

import com.example.sra.entity.Company;
import com.example.sra.entity.JobDescription;
import com.example.sra.entity.Skill;
import com.example.sra.entity.User;
import com.example.sra.repository.CompanyRepository;
import com.example.sra.repository.JobDescriptionRepository;
import com.example.sra.repository.SkillRepository;
import com.example.sra.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository,
                               CompanyRepository companyRepository,
                               JobDescriptionRepository jobDescriptionRepository,
                               SkillRepository skillRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default Admin if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                    "admin",
                    "admin@cvlens.com",
                    passwordEncoder.encode("admin123"),
                    "ROLE_ADMIN"
            );
            userRepository.save(admin);
            System.out.println("Default Admin seeded: admin / admin123");
        }

        // Seed default User if not exists (for easy testing/viva demonstration)
        if (userRepository.findByUsername("john_doe").isEmpty()) {
            User testUser = new User(
                    "john_doe",
                    "john@example.com",
                    passwordEncoder.encode("user123"),
                    "ROLE_USER"
            );
            userRepository.save(testUser);
            System.out.println("Demo User seeded: john_doe / user123");
        }

        // Seed sample companies and roles if database is empty
        if (companyRepository.count() == 0) {
            // Company 1: Google
            Company google = companyRepository.save(new Company("Google"));
            
            // Role 1
            JobDescription swe = jobDescriptionRepository.save(new JobDescription(
                    google, 
                    "Software Engineer", 
                    "We are looking for a Software Engineer with solid backend development expertise. You will work on writing clean and scalable code."
            ));
            skillRepository.saveAll(Arrays.asList(
                    new Skill("Java", swe),
                    new Skill("Spring Boot", swe),
                    new Skill("SQL", swe),
                    new Skill("Git", swe),
                    new Skill("Hibernate", swe)
            ));

            // Role 2
            JobDescription da = jobDescriptionRepository.save(new JobDescription(
                    google,
                    "Data Analyst",
                    "Join our metrics team. You will gather business intelligence, write complex database queries, and compile reports."
            ));
            skillRepository.saveAll(Arrays.asList(
                    new Skill("Python", da),
                    new Skill("SQL", da),
                    new Skill("Excel", da),
                    new Skill("PowerBI", da),
                    new Skill("Statistics", da)
            ));

            // Company 2: Microsoft
            Company microsoft = companyRepository.save(new Company("Microsoft"));
            JobDescription backend = jobDescriptionRepository.save(new JobDescription(
                    microsoft,
                    "Backend Developer",
                    "We are building modern cloud platforms. We need developers who understand REST APIs, C#, and Docker containers."
            ));
            skillRepository.saveAll(Arrays.asList(
                    new Skill("C#", backend),
                    new Skill("dotNET", backend),
                    new Skill("SQL", backend),
                    new Skill("Docker", backend),
                    new Skill("Azure", backend)
            ));

            // Company 3: Amazon
            Company amazon = companyRepository.save(new Company("Amazon"));
            JobDescription cloud = jobDescriptionRepository.save(new JobDescription(
                    amazon,
                    "Cloud Engineer",
                    "Manage massive distributed cloud solutions. Write scripts to automate infrastructure and provision containers."
            ));
            skillRepository.saveAll(Arrays.asList(
                    new Skill("AWS", cloud),
                    new Skill("Linux", cloud),
                    new Skill("Python", cloud),
                    new Skill("Terraform", cloud),
                    new Skill("Docker", cloud)
            ));

            System.out.println("Default Companies, Job Roles, and Skills seeded!");
        }
    }
}
