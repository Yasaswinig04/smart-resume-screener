package com.resume.screener;

public class LLMMatchResult {

    private final boolean successful;
    private final double score;
    private final String justification;

    public LLMMatchResult(
            boolean successful,
            double score,
            String justification) {

        this.successful = successful;
        this.score = score;
        this.justification = justification;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public double getScore() {
        return score;
    }

    public String getJustification() {
        return justification;
    }
}
