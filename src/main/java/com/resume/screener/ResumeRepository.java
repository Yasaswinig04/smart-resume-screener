package com.resume.screener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResumeRepository {

    public int save(Resume resume, String rawText) {

        String sql = """
                INSERT INTO resumes
                (name, skills, experience, education, raw_text)
                VALUES (?, ?, ?, ?, ?)
                """;

        String skills = String.join(
                ", ",
                resume.getSkills()
        );

        String experience =
                resume.getExperience();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                java.sql.Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    resume.getName()
            );

            statement.setString(
                    2,
                    skills
            );

            statement.setString(
                    3,
                    experience
            );

            statement.setString(
                    4,
                    resume.getEducation()
            );

            statement.setString(
                    5,
                    rawText
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save resume.",
                    e
            );
        }

        return -1;
    }

    public int saveJob(
            JobDescription job) {

        String sql = """
                INSERT INTO job_descriptions
                (title, description)
                VALUES (?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                java.sql.Statement.RETURN_GENERATED_KEYS
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
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save job description.",
                    e
            );
        }

        return -1;
    }

    public int saveMatchResult(
            int resumeId,
            int jobId,
            double score,
            String justification) {

        String sql = """
                INSERT INTO match_results
                (resume_id, job_id, score, justification)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                java.sql.Statement.RETURN_GENERATED_KEYS
                        )
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
                    score
            );

            statement.setString(
                    4,
                    justification
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save match result.",
                    e
            );
        }

        return -1;
    }
}
