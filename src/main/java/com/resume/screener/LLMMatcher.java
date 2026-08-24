package com.resume.screener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMMatcher {

    private static final String API_URL =
            "https://api.openai.com/v1/responses";

    private final HttpClient client;

    public LLMMatcher() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public LLMMatchResult match(
            Resume resume,
            JobDescription job) throws Exception {

        String apiKey =
                System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            return new LLMMatchResult(
                    false,
                    0.0,
                    "LLM matching is configured but "
                            + "OPENAI_API_KEY is not set."
            );
        }

        String model =
                System.getenv("OPENAI_MODEL");

        if (model == null || model.isBlank()) {
            model = "gpt-4.1-mini";
        }

        String prompt = buildPrompt(resume, job);

        String requestBody =
                "{"
                        + "\"model\":\""
                        + escape(model)
                        + "\","
                        + "\"input\":\""
                        + escape(prompt)
                        + "\""
                        + "}";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .timeout(Duration.ofSeconds(60))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(requestBody)
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            System.err.println(
                    "LLM API response: "
                            + response.body()
            );

            return new LLMMatchResult(
                    false,
                    0.0,
                    "LLM request failed. HTTP "
                            + response.statusCode()
            );
        }

        String output =
                extractText(response.body());

        double score =
                extractScore(output);

        if (score < 1.0 || score > 10.0) {
            return new LLMMatchResult(
                    false,
                    0.0,
                    "LLM response did not contain "
                            + "a valid 1-10 score."
            );
        }

        String justification =
                extractJustification(output);

        return new LLMMatchResult(
                true,
                score,
                justification
        );
    }

    private String buildPrompt(
            Resume resume,
            JobDescription job) {

        return """
                Compare the following candidate resume
                against the following job description.

                Evaluate the candidate based on:

                - Required technical skills
                - Relevant experience
                - Education
                - Projects
                - Overall relevance to the role

                Do not invent skills, experience, education,
                projects, or achievements.

                Give a score from 1 to 10.

                Return exactly this format:

                SCORE: <number from 1 to 10>
                JUSTIFICATION: <2 to 4 concise sentences>

                CANDIDATE

                Name:
                %s

                Summary:
                %s

                Skills:
                %s

                Experience:
                %s

                Education:
                %s

                Projects:
                %s

                Certifications:
                %s

                JOB DESCRIPTION

                Title:
                %s

                Description:
                %s
                """.formatted(
                resume.getName(),
                resume.getSummary(),
                String.join(
                        ", ",
                        resume.getSkills()
                ),
                resume.getExperience(),
                resume.getEducation(),
                resume.getProjects(),
                resume.getCertifications(),
                job.getTitle(),
                job.getDescription()
        );
    }

    private String extractText(String json) {

        Pattern pattern =
                Pattern.compile(
                        "\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                );

        Matcher matcher =
                pattern.matcher(json);

        StringBuilder result =
                new StringBuilder();

        while (matcher.find()) {

            String value =
                    matcher.group(1)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");

            result.append(value)
                    .append("\n");
        }

        if (result.length() == 0) {
            return json;
        }

        return result.toString().trim();
    }

    private double extractScore(String text) {

        Pattern pattern =
                Pattern.compile(
                        "SCORE\\s*:\\s*"
                                + "(10(?:\\.0)?|"
                                + "[1-9](?:\\.\\d+)?)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (!matcher.find()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(
                    matcher.group(1)
            );
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String extractJustification(
            String text) {

        Pattern pattern =
                Pattern.compile(
                        "JUSTIFICATION\\s*:\\s*(.*)",
                        Pattern.CASE_INSENSITIVE
                        | Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return text.trim();
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
