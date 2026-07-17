package com.example.sra.repository;

import com.example.sra.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"resume.user", "company", "jobDescription"})
    List<AnalysisReport> findByResumeUserIdOrderByAnalyzedAtDesc(Long userId);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"resume.user", "company", "jobDescription"})
    List<AnalysisReport> findFirst5ByResumeUserIdOrderByAnalyzedAtDesc(Long userId);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"resume.user", "company", "jobDescription"})
    List<AnalysisReport> findFirst5ByOrderByAnalyzedAtDesc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"resume.user", "company", "jobDescription"})
    List<AnalysisReport> findAll();
    
    @Query("SELECT AVG(a.score) FROM AnalysisReport a")
    Double getAverageScore();
    
    @Query("SELECT AVG(a.score) FROM AnalysisReport a WHERE a.resume.user.id = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);
    
    @Query("SELECT MAX(a.score) FROM AnalysisReport a WHERE a.resume.user.id = :userId")
    Integer getHighestScoreByUserId(@Param("userId") Long userId);
}
