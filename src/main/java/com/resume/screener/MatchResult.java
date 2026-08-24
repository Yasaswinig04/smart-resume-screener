package com.resume.screener;

import java.util.List;

public class MatchResult {

    private final double score;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final String justification;

    public MatchResult(
            double score,
            List<String> matchedSkills,
            List<String> missingSkills,
            String justification) {

        this.score = score;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.justification = justification;
    }

    public double getScore() {
        return score;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public String getJustification() {
        return justification;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("Match Score: ")
                .append(String.format("%.1f", score))
                .append("/10\n\n");

        result.append("Matched Skills:\n");

        if (matchedSkills.isEmpty()) {
            result.append("- None\n");
        } else {
            for (String skill : matchedSkills) {
                result.append("- ")
                        .append(skill)
                        .append("\n");
            }
        }

        result.append("\nMissing Skills:\n");

        if (missingSkills.isEmpty()) {
            result.append("- None\n");
        } else {
            for (String skill : missingSkills) {
                result.append("- ")
                        .append(skill)
                        .append("\n");
            }
        }

        result.append("\nJustification:\n")
                .append(justification);

        return result.toString();
    }
}
