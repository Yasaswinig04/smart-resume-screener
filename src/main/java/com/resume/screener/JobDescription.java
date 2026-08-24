package com.resume.screener;

import java.util.List;

public class JobDescription {

    private final String title;
    private final String description;
    private final List<String> requiredSkills;

    public JobDescription(
            String title,
            String description,
            List<String> requiredSkills) {

        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("Job Title:\n")
                .append(title)
                .append("\n\n");

        result.append("Required Skills:\n");

        for (String skill : requiredSkills) {
            result.append("- ")
                    .append(skill)
                    .append("\n");
        }

        result.append("\nDescription:\n")
                .append(description);

        return result.toString();
    }
}
