package com.resume.screener;

public class CandidateResult {

    private final String resumeFile;
    private final Resume resume;
    private final MatchResult matchResult;

    public CandidateResult(
            String resumeFile,
            Resume resume,
            MatchResult matchResult) {

        this.resumeFile = resumeFile;
        this.resume = resume;
        this.matchResult = matchResult;
    }

    public String getResumeFile() {
        return resumeFile;
    }

    public Resume getResume() {
        return resume;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }
}
