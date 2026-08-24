package com.resume.screener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobDescriptionParser {

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

    public JobDescription parse(
            String title,
            String description) {

        List<String> requiredSkills =
                extractSkills(description);

        return new JobDescription(
                title,
                description,
                requiredSkills
        );
    }

    private List<String> extractSkills(
            String description) {

        String lowerText =
                description.toLowerCase();

        List<String> skills =
                new ArrayList<>();

        for (String skill : KNOWN_SKILLS) {

            if (lowerText.contains(
                    skill.toLowerCase())) {

                skills.add(skill);
            }
        }

        return skills;
    }
}
