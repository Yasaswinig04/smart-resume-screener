package com.resume.screener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResumeParser {

    private static final List<String> KNOWN_SKILLS =
            Arrays.asList(
                    "java",
                    "python",
                    "javascript",
                    "typescript",
                    "c++",
                    "sql",
                    "spring boot",
                    "rest api",
                    "git",
                    "github",
                    "aws",
                    "docker",
                    "react",
                    "node.js",
                    "html",
                    "css",
                    "mongodb",
                    "mysql",
                    "postgresql",
                    "machine learning",
                    "deep learning",
                    "tensorflow",
                    "pytorch",
                    "tenseal",
                    "flask",
                    "fastapi",
                    "linux",
                    "data structures",
                    "algorithms",
                    "object-oriented programming"
            );

    public Resume parse(String text) {

        String name =
                extractName(text);

        String summary =
                extractSection(
                        text,
                        "PROFESSIONAL SUMMARY",
                        "EDUCATION"
                );

        String education =
                extractSection(
                        text,
                        "EDUCATION",
                        "TECHNICAL SKILLS"
                );

        String skillsSection =
                extractSection(
                        text,
                        "TECHNICAL SKILLS",
                        "PROJECTS"
                );

        String projects =
                extractSection(
                        text,
                        "PROJECTS",
                        "CERTIFICATIONS & ACHIEVEMENTS"
                );

        String certifications =
                extractSection(
                        text,
                        "CERTIFICATIONS & ACHIEVEMENTS",
                        "LEADERSHIP & ACTIVITIES"
                );

        String leadership =
                extractSection(
                        text,
                        "LEADERSHIP & ACTIVITIES",
                        "LANGUAGES"
                );

        String languages =
                extractSection(
                        text,
                        "LANGUAGES",
                        null
                );

        List<String> skills =
                extractSkills(
                        text,
                        skillsSection
                );

        /*
         * This resume does not have a separate
         * EXPERIENCE heading. Its PROJECTS section
         * represents the candidate's practical
         * experience for screening purposes.
         */
        String experience = projects;

        return new Resume(
                name,
                summary,
                skills,
                experience,
                education,
                projects,
                certifications,
                leadership,
                languages
        );
    }

    private String extractName(String text) {

        String[] lines =
                text.split("\\R");

        for (String line : lines) {

            String trimmed =
                    line.trim();

            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        return "Unknown";
    }

    private List<String> extractSkills(
            String fullText,
            String skillsSection) {

        String textToSearch;

        if (skillsSection != null
                && !skillsSection.isBlank()) {

            textToSearch =
                    skillsSection;

        } else {

            textToSearch =
                    fullText;
        }

        String lowerText =
                textToSearch.toLowerCase();

        List<String> skills =
                new ArrayList<>();

        for (String skill :
                KNOWN_SKILLS) {

            if (lowerText.contains(
                    skill.toLowerCase()
            )) {

                skills.add(skill);
            }
        }

        return skills;
    }

    private String extractSection(
            String text,
            String startMarker,
            String endMarker) {

        String upperText =
                text.toUpperCase();

        int start =
                upperText.indexOf(
                        startMarker.toUpperCase()
                );

        if (start == -1) {
            return "";
        }

        start +=
                startMarker.length();

        int end;

        if (endMarker == null) {

            end =
                    text.length();

        } else {

            end =
                    upperText.indexOf(
                            endMarker.toUpperCase(),
                            start
                    );

            if (end == -1) {
                end =
                        text.length();
            }
        }

        return text.substring(
                start,
                end
        ).trim();
    }
}
