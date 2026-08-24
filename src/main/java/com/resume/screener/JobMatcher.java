package com.resume.screener;

import java.util.ArrayList;
import java.util.List;

public class JobMatcher {

    public MatchResult match(
            Resume resume,
            JobDescription job) {

        List<String> resumeSkills =
                resume.getSkills();

        List<String> requiredSkills =
                job.getRequiredSkills();

        List<String> matchedSkills =
                new ArrayList<>();

        List<String> missingSkills =
                new ArrayList<>();

        for (String required : requiredSkills) {

            boolean matched = false;

            for (String candidateSkill : resumeSkills) {

                if (candidateSkill.equalsIgnoreCase(required)
                        || isRelated(candidateSkill, required)) {

                    matched = true;
                    break;
                }
            }

            if (matched) {
                matchedSkills.add(required);
            } else {
                missingSkills.add(required);
            }
        }

        double skillScore =
                calculateSkillScore(
                        requiredSkills.size(),
                        matchedSkills.size()
                );

        double projectScore =
                calculateTextRelevance(
                        resume.getProjects(),
                        job.getDescription()
                );

        double summaryScore =
                calculateTextRelevance(
                        resume.getSummary(),
                        job.getDescription()
                );

        double finalScore =
                (skillScore * 0.70)
                        + (projectScore * 0.20)
                        + (summaryScore * 0.10);

        finalScore =
                Math.min(10.0, finalScore);

        String justification =
                buildJustification(
                        matchedSkills,
                        missingSkills,
                        finalScore
                );

        return new MatchResult(
                finalScore,
                matchedSkills,
                missingSkills,
                justification
        );
    }

    private double calculateSkillScore(
            int total,
            int matched) {

        if (total == 0) {
            return 0.0;
        }

        return (matched * 10.0) / total;
    }

    private double calculateTextRelevance(
            String candidateText,
            String jobText) {

        if (candidateText == null
                || candidateText.isBlank()
                || jobText == null
                || jobText.isBlank()) {

            return 0.0;
        }

        String lowerCandidate =
                candidateText.toLowerCase();

        String lowerJob =
                jobText.toLowerCase();

        String[] words =
                lowerJob.split("[^a-z0-9+#.]+");

        int totalWords = 0;
        int matchedWords = 0;

        for (String word : words) {

            if (word.length() < 4) {
                continue;
            }

            totalWords++;

            if (lowerCandidate.contains(word)) {
                matchedWords++;
            }
        }

        if (totalWords == 0) {
            return 0.0;
        }

        return Math.min(
                10.0,
                (matchedWords * 10.0) / totalWords
        );
    }

    private boolean isRelated(
            String candidate,
            String required) {

        String a = candidate.toLowerCase();
        String b = required.toLowerCase();

        if ((a.equals("github") && b.equals("git"))
                || (a.equals("git") && b.equals("github"))) {
            return true;
        }

        if ((a.equals("node.js") && b.equals("javascript"))
                || (a.equals("javascript") && b.equals("node.js"))) {
            return true;
        }

        if ((a.equals("spring boot")
                && b.equals("java"))
                || (a.equals("java")
                && b.equals("spring boot"))) {
            return true;
        }

        return false;
    }

    private String buildJustification(
            List<String> matched,
            List<String> missing,
            double score) {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "Candidate achieved a "
        );

        result.append(
                String.format("%.1f", score)
        );

        result.append(
                "/10 overall fit score. "
        );

        if (!matched.isEmpty()) {

            result.append(
                    "Strong matches include: "
            );

            result.append(
                    String.join(", ", matched)
            );

            result.append(". ");
        }

        if (!missing.isEmpty()) {

            result.append(
                    "Potential gaps include: "
            );

            result.append(
                    String.join(", ", missing)
            );

            result.append(".");
        }

        return result.toString();
    }
}
