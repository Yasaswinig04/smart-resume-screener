package com.resume.screener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println();
        System.out.println("=================================");
        System.out.println("      SMART RESUME SCREENER");
        System.out.println("=================================");
        System.out.println();

        Path resumeDirectory =
                Paths.get("sample_data/resumes");

        Path jobPath =
                Paths.get(
                        "sample_data/job_description.txt"
                );

        try {

            // -------------------------------------------------
            // 1. Initialize database
            // -------------------------------------------------

            DatabaseManager.initializeDatabase();

            // -------------------------------------------------
            // 2. Validate input files
            // -------------------------------------------------

            if (!Files.exists(resumeDirectory)) {

                System.err.println(
                        "Resume directory does not exist: "
                                + resumeDirectory
                );

                return;
            }

            if (!Files.exists(jobPath)) {

                System.err.println(
                        "Job description does not exist: "
                                + jobPath
                );

                return;
            }

            // -------------------------------------------------
            // 3. Load job description
            // -------------------------------------------------

            String jobText =
                    Files.readString(jobPath);

            JobDescriptionParser jobParser =
                    new JobDescriptionParser();

            JobDescription job =
                    jobParser.parse(
                            "Software Engineering Position",
                            jobText
                    );

            System.out.println(
                    "Job description loaded."
            );

            System.out.println(
                    "Required skills: "
                            + job.getRequiredSkills()
            );

            // -------------------------------------------------
            // 4. Screen all resumes
            // -------------------------------------------------

            BatchScreener screener =
                    new BatchScreener();

            List<CandidateResult> results =
                    screener.screen(
                            resumeDirectory,
                            job
                    );

            // -------------------------------------------------
            // 5. Display complete ranking
            // -------------------------------------------------

            System.out.println();

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "        CANDIDATE RANKING"
            );

            System.out.println(
                    "================================="
            );

            if (results.isEmpty()) {

                System.out.println(
                        "No PDF resumes found."
                );

            } else {

                int rank = 1;

                for (CandidateResult candidate :
                        results) {

                    MatchResult match =
                            candidate
                                    .getMatchResult();

                    System.out.println();

                    System.out.println(
                            rank
                                    + ". "
                                    + candidate
                                            .getResume()
                                            .getName()
                    );

                    System.out.println(
                            "   File: "
                                    + candidate
                                            .getResumeFile()
                    );

                    System.out.println(
                            "   Score: "
                                    + String.format(
                                            "%.1f",
                                            match.getScore()
                                    )
                                    + "/10"
                    );

                    System.out.println(
                            "   Matched skills: "
                                    + match
                                            .getMatchedSkills()
                    );

                    System.out.println(
                            "   Missing skills: "
                                    + match
                                            .getMissingSkills()
                    );

                    System.out.println(
                            "   Justification: "
                                    + match
                                            .getJustification()
                    );

                    rank++;
                }
            }

            // -------------------------------------------------
            // 6. Display top-3 shortlist
            // -------------------------------------------------

            List<CandidateResult> shortlist =
                    screener.shortlist(
                            results,
                            3
                    );

            System.out.println();

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "          TOP CANDIDATES"
            );

            System.out.println(
                    "================================="
            );

            if (shortlist.isEmpty()) {

                System.out.println(
                        "No candidates available."
                );

            } else {

                int position = 1;

                for (CandidateResult candidate :
                        shortlist) {

                    System.out.println();

                    System.out.println(
                            position
                                    + ". "
                                    + candidate
                                            .getResume()
                                            .getName()
                    );

                    System.out.println(
                            "   Score: "
                                    + String.format(
                                            "%.1f",
                                            candidate
                                                    .getMatchResult()
                                                    .getScore()
                                    )
                                    + "/10"
                    );

                    System.out.println(
                            "   Reason: "
                                    + candidate
                                            .getMatchResult()
                                            .getJustification()
                    );

                    position++;
                }
            }

            // -------------------------------------------------
            // 7. LLM semantic matching
            // -------------------------------------------------

            System.out.println();

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "       LLM MATCHING STATUS"
            );

            System.out.println(
                    "================================="
            );

            if (!results.isEmpty()) {

                CandidateResult topCandidate =
                        results.get(0);

                LLMMatcher llmMatcher =
                        new LLMMatcher();

                LLMMatchResult llmResult =
                        llmMatcher.match(
                                topCandidate.getResume(),
                                job
                        );

                if (llmResult.isSuccessful()) {

                    System.out.println(
                            "LLM semantic score: "
                                    + String.format(
                                            "%.1f",
                                            llmResult.getScore()
                                    )
                                    + "/10"
                    );

                    System.out.println(
                            "LLM justification:"
                    );

                    System.out.println(
                            llmResult
                                    .getJustification()
                    );

                } else {

                    System.out.println(
                            "LLM matching is not active."
                    );

                    System.out.println(
                            llmResult
                                    .getJustification()
                    );
                }
            }

            // -------------------------------------------------
            // 8. Completion
            // -------------------------------------------------

            System.out.println();

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "       SCREENING COMPLETE"
            );

            System.out.println(
                    "================================="
            );

            System.out.println();

        } catch (Exception e) {

            System.err.println();

            System.err.println(
                    "ERROR: Screening failed."
            );

            e.printStackTrace();
        }
    }
}
