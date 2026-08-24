package com.resume.screener;

import java.util.List;

public class Resume {

    private final String name;
    private final String summary;
    private final List<String> skills;
    private final String experience;
    private final String education;
    private final String projects;
    private final String certifications;
    private final String leadership;
    private final String languages;

    /*
     * Constructor used by the current ResumeParser.
     * It supplies 8 values.
     */
    public Resume(
            String name,
            String summary,
            List<String> skills,
            String experience,
            String education,
            String projects,
            String certifications,
            String leadership) {

        this(
                name,
                summary,
                skills,
                experience,
                education,
                projects,
                certifications,
                leadership,
                ""
        );
    }

    /*
     * Full constructor with languages.
     */
    public Resume(
            String name,
            String summary,
            List<String> skills,
            String experience,
            String education,
            String projects,
            String certifications,
            String leadership,
            String languages) {

        this.name = name;
        this.summary = summary;
        this.skills = skills;
        this.experience = experience;
        this.education = education;
        this.projects = projects;
        this.certifications = certifications;
        this.leadership = leadership;
        this.languages = languages;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getSkills() {
        return skills;
    }

    public String getExperience() {
        return experience;
    }

    public String getEducation() {
        return education;
    }

    public String getProjects() {
        return projects;
    }

    public String getCertifications() {
        return certifications;
    }

    public String getLeadership() {
        return leadership;
    }

    public String getLanguages() {
        return languages;
    }

    @Override
    public String toString() {

        return "Name: " + name
                + "\n\nProfessional Summary:\n"
                + summary
                + "\n\nSkills:\n"
                + String.join(", ", skills)
                + "\n\nExperience:\n"
                + experience
                + "\n\nEducation:\n"
                + education
                + "\n\nProjects:\n"
                + projects
                + "\n\nCertifications & Achievements:\n"
                + certifications
                + "\n\nLeadership & Activities:\n"
                + leadership
                + "\n\nLanguages:\n"
                + languages;
    }
}
