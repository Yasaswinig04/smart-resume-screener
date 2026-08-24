package com.resume.screener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BatchScreener {

    private final PdfResumeExtractor extractor;
    private final ResumeParser resumeParser;
    private final JobMatcher matcher;
    private final LLMMatcher llmMatcher;

    public BatchScreener() {
        this.extractor = new PdfResumeExtractor();
        this.resumeParser = new ResumeParser();
        this.matcher = new JobMatcher();
        this.llmMatcher = new LLMMatcher();
    }

    public List<CandidateResult> screen(
            Path resumeDirectory,
            JobDescription job)
            throws IOException {

        List<CandidateResult> results =
                new ArrayList<>();

        try (Stream<Path> files =
                     Files.list(resumeDirectory)) {

            files
                    .filter(Files::isRegularFile)
                    .filter(this::isPdf)
                    .forEach(file -> {

                        try {
                            String text =
                                    extractor.extractText(file);

                            Resume resume =
                                    resumeParser.parse(text);

                            MatchResult matchResult =
                                    createMatchResult(
                                            resume,
                                            job
                                    );

                            results.add(
                                    new CandidateResult(
                                            file.getFileName()
                                                    .toString(),
                                            resume,
                                            matchResult
                                    )
                            );

                        } catch (Exception e) {

                            System.err.println(
                                    "Could not process: "
                                            + file.getFileName()
                            );

                            e.printStackTrace();
                        }
                    });
        }

        results.sort(
                Comparator.comparingDouble(
                        (CandidateResult result) ->
                                result.getMatchResult()
                                        .getScore()
                ).reversed()
        );

        return results;
    }

    private MatchResult createMatchResult(
            Resume resume,
            JobDescription job) {

        MatchResult ruleBasedResult =
                matcher.match(resume, job);

        try {
            LLMMatchResult llmResult =
                    llmMatcher.match(resume, job);

            if (llmResult.isSuccessful()) {

                return new MatchResult(
                        llmResult.getScore(),
                        ruleBasedResult.getMatchedSkills(),
                        ruleBasedResult.getMissingSkills(),
                        llmResult.getJustification()
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "LLM matching failed for "
                            + resume.getName()
                            + ". Using rule-based fallback."
            );
        }

        return ruleBasedResult;
    }

    private boolean isPdf(Path file) {

        String fileName =
                file.getFileName()
                        .toString()
                        .toLowerCase();

        return fileName.endsWith(".pdf");
    }

    public List<CandidateResult> shortlist(
            List<CandidateResult> results,
            int limit) {

        int count =
                Math.min(limit, results.size());

        return new ArrayList<>(
                results.subList(0, count)
        );
    }
}
