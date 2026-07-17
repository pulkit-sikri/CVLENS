package com.example.sra.dto;

public class ResumeSubmissionDto {
    private Long userId;
    private Long companyId;
    private Long jobDescriptionId;
    private String resumeContent;

    public ResumeSubmissionDto() {
    }

    public ResumeSubmissionDto(Long userId, Long companyId, Long jobDescriptionId, String resumeContent) {
        this.userId = userId;
        this.companyId = companyId;
        this.jobDescriptionId = jobDescriptionId;
        this.resumeContent = resumeContent;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getJobDescriptionId() {
        return jobDescriptionId;
    }

    public void setJobDescriptionId(Long jobDescriptionId) {
        this.jobDescriptionId = jobDescriptionId;
    }

    public String getResumeContent() {
        return resumeContent;
    }

    public void setResumeContent(String resumeContent) {
        this.resumeContent = resumeContent;
    }
}
