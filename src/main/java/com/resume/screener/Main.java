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

            DatabaseManager.initializeDatabase();

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

            ResumeRepository resumeRepository =
                    new ResumeRepository();

            int jobId =
                    saveJobDescription(job);

            System.out.println(
                    "Job description stored. ID: "
                            + jobId
            );

            BatchScreener screener =
                    new BatchScreener();

            List<CandidateResult> results =
                    screener.screen(
                            resumeDirectory,
                            job
                    );

            for (CandidateResult candidate :
                    results) {

                try {

                    PdfResumeExtractor extractor =
                            new PdfResumeExtractor();

                    Path resumePath =
                            resumeDirectory.resolve(
                                    candidate.getResumeFile()
                            );

                    String rawText =
                            extractor.extractText(
                                    resumePath
                            );

                    int resumeId =
                            resumeRepository.save(
                                    candidate.getResume(),
                                    rawText
                            );

                    System.out.println(
                            "Stored candidate: "
                                    + candidate
                                            .getResume()
                                            .getName()
                    );

                    saveMatchResult(
                            resumeId,
                            jobId,
                            candidate.getMatchResult()
                    );

                } catch (Exception e) {

                    System.err.println(
                            "Could not store candidate: "
                                    + candidate
                                            .getResumeFile()
                    );

                    e.printStackTrace();
                }
            }

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
                                            candidate
                                                    .getMatchResult()
                                                    .getScore()
                                    )
                                    + "/10"
                    );

                    System.out.println(
                            "   Matched skills: "
                                    + candidate
                                            .getMatchResult()
                                            .getMatchedSkills()
                    );

                    System.out.println(
                            "   Missing skills: "
                                    + candidate
                                            .getMatchResult()
                                            .getMissingSkills()
                    );

                    System.out.println(
                            "   Justification: "
                                    + candidate
                                            .getMatchResult()
                                            .getJustification()
                    );

                    rank++;
                }
            }

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

            List<CandidateResult> shortlisted =
                    screener.shortlist(
                            results,
                            3
                    );

            if (shortlisted.isEmpty()) {

                System.out.println(
                        "No candidates available."
                );

            } else {

                int rank = 1;

                for (CandidateResult candidate :
                        shortlisted) {

                    System.out.println();

                    System.out.println(
                            rank
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

                    rank++;
                }
            }

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

        } catch (Exception e) {

            System.err.println();
            System.err.println(
                    "ERROR: Screening failed."
            );

            e.printStackTrace();
        }
    }

    private static int saveJobDescription(
            JobDescription job) {

        String sql =
                """
                INSERT INTO job_descriptions
                (title, description)
                VALUES (?, ?)
                """;

        try (
                java.sql.Connection connection =
                        DatabaseManager.getConnection();

                java.sql.PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                java.sql.Statement
                                        .RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    job.getTitle()
            );

            statement.setString(
                    2,
                    job.getDescription()
            );

            statement.executeUpdate();

            try (
                    java.sql.ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (java.sql.SQLException e) {

            throw new RuntimeException(
                    "Failed to save job description.",
                    e
            );
        }

        return -1;
    }

    private static void saveMatchResult(
            int resumeId,
            int jobId,
            MatchResult result) {

        String sql =
                """
                INSERT INTO match_results
                (resume_id, job_id, score, justification)
                VALUES (?, ?, ?, ?)
                """;

        try (
                java.sql.Connection connection =
                        DatabaseManager.getConnection();

                java.sql.PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    resumeId
            );

            statement.setInt(
                    2,
                    jobId
            );

            statement.setDouble(
                    3,
                    result.getScore()
            );

            statement.setString(
                    4,
                    result.getJustification()
            );

            statement.executeUpdate();

        } catch (java.sql.SQLException e) {

            throw new RuntimeException(
                    "Failed to save match result.",
                    e
            );
        }
    }
}
