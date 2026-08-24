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
    private final ResumeRepository repository;

    public BatchScreener() {

        this.extractor =
                new PdfResumeExtractor();

        this.resumeParser =
                new ResumeParser();

        this.matcher =
                new JobMatcher();

        this.repository =
                new ResumeRepository();
    }

    public List<CandidateResult> screen(
            Path resumeDirectory,
            JobDescription job)
            throws IOException {

        List<CandidateResult> results =
                new ArrayList<>();

        /*
         * Store the job description once.
         */
        int jobId =
                repository.saveJob(job);

        System.out.println(
                "Job description stored. ID: "
                        + jobId
        );

        try (Stream<Path> files =
                     Files.list(resumeDirectory)) {

            files
                    .filter(Files::isRegularFile)
                    .filter(this::isPdf)
                    .forEach(file -> {

                        try {

                            /*
                             * 1. Extract PDF text
                             */
                            String text =
                                    extractor.extractText(
                                            file
                                    );

                            /*
                             * 2. Parse resume
                             */
                            Resume resume =
                                    resumeParser.parse(
                                            text
                                    );

                            /*
                             * 3. Store resume
                             */
                            int resumeId =
                                    repository.save(
                                            resume,
                                            text
                                    );

                            /*
                             * 4. Match resume against job
                             */
                            MatchResult match =
                                    matcher.match(
                                            resume,
                                            job
                                    );

                            /*
                             * 5. Store match result
                             */
                            repository.saveMatchResult(
                                    resumeId,
                                    jobId,
                                    match.getScore(),
                                    match.getJustification()
                            );

                            /*
                             * 6. Add candidate to ranking
                             */
                            results.add(
                                    new CandidateResult(
                                            file.getFileName()
                                                    .toString(),
                                            resume,
                                            match
                                    )
                            );

                            System.out.println(
                                    "Stored candidate: "
                                            + resume.getName()
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

        /*
         * Highest score first.
         */
        results.sort(
                Comparator.comparingDouble(
                        (CandidateResult result) ->
                                result.getMatchResult()
                                        .getScore()
                ).reversed()
        );

        return results;
    }

    /*
     * Return the top N candidates.
     */
    public List<CandidateResult> shortlist(
            List<CandidateResult> results,
            int limit) {

        int count =
                Math.min(
                        limit,
                        results.size()
                );

        return new ArrayList<>(
                results.subList(0, count)
        );
    }

    /*
     * Return candidates meeting a minimum score.
     */
    public List<CandidateResult> shortlistByScore(
            List<CandidateResult> results,
            double minimumScore) {

        List<CandidateResult> shortlisted =
                new ArrayList<>();

        for (CandidateResult candidate :
                results) {

            if (candidate
                    .getMatchResult()
                    .getScore()
                    >= minimumScore) {

                shortlisted.add(candidate);
            }
        }

        return shortlisted;
    }

    private boolean isPdf(Path file) {

        String fileName =
                file.getFileName()
                        .toString()
                        .toLowerCase();

        return fileName.endsWith(".pdf");
    }
}
