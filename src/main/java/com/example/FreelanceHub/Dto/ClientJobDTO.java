package com.example.FreelanceHub.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClientJobDTO {

    @NotBlank(message = "Job Title is required")
    @Size(min = 10, message = "Job Title must be atleast 10 characters")
    @Size(max = 100, message = "Job Title must not exceed 100 characters")
    private String JobTitle;

    @NotBlank(message = "Job Description is required")
    @Size(min = 20, message = "Job Title must be atleast 20 characters")
    @Size(max = 500, message = "Job Description must not exceed 500 characters")
    private String jobDesc;

    @NotBlank(message = "Skill requirements are mandatory")
    private String SkillReq;

    @NotNull(message = "Minimum duration is required")
    @Min(value = 1, message = "Minimum duration must be at least 1 day")
    private Integer DurMin;

    @NotNull(message = "Maximum duration is required")
    @Min(value = 1, message = "Maximum duration must be at least 1 day")
    private Integer DurMax;

    @NotNull(message = "Minimum cost is required")
    @Min(value = 1, message = "Minimum cost must be at least 1 dollar")
    private Integer CostMin;

    @NotNull(message = "Maximum cost is required")
    @Min(value = 1, message = "Maximum cost must be at least 1 dollar")
    private Integer CostMax;

    @NotNull(message = "Experience is required")
    @Min(value = 1, message = "Minimum experience must be at least 1 year")
    private Integer ExpMin;

    @NotBlank(message = "Job status is required")
    private String JobStat;

    public String getJobTitle() {
        return JobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.JobTitle = jobTitle;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getSkillReq() {
        return SkillReq;
    }

    public void setSkillReq(String skillReq) {
        this.SkillReq = skillReq;
    }

    public Integer getDurMin() {
        return DurMin;
    }

    public void setDurMin(Integer durMin) {
        this.DurMin = durMin;
    }

    public Integer getDurMax() {
        return DurMax;
    }

    public void setDurMax(Integer durMax) {
        this.DurMax = durMax;
    }

    public Integer getCostMin() {
        return CostMin;
    }

    public void setCostMin(Integer costMin) {
        this.CostMin = costMin;
    }

    public Integer getCostMax() {
        return CostMax;
    }

    public void setCostMax(Integer costMax) {
        this.CostMax = costMax;
    }

    public Integer getExpMin() {
        return ExpMin;
    }

    public void setExpMin(Integer expMin) {
        this.ExpMin = expMin;
    }

    public String getJobStat() {
        return JobStat;
    }

    public void setJobStat(String jobStat) {
        this.JobStat = jobStat;
    }
}

