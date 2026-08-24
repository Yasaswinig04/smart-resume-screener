package com.resume.screener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ApiServer {

    private static final int PORT = 8080;

    private static final Path RESUME_DIRECTORY =
            Paths.get("sample_data/resumes");

    private static final Path JOB_FILE =
            Paths.get("sample_data/job_description.txt");

    public static void main(String[] args)
            throws Exception {

        DatabaseManager.initializeDatabase();

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(PORT),
                        0
                );

        server.createContext(
                "/health",
                ApiServer::health
        );

        server.createContext(
                "/screen",
                ApiServer::screen
        );

        server.createContext(
                "/shortlist",
                ApiServer::shortlist
        );

        server.setExecutor(null);

        System.out.println();
        System.out.println(
                "================================="
        );
        System.out.println(
                " SMART RESUME SCREENER API"
        );
        System.out.println(
                "================================="
        );
        System.out.println(
                "Server running on port "
                        + PORT
        );
        System.out.println();
        System.out.println(
                "GET /health"
        );
        System.out.println(
                "GET /screen"
        );
        System.out.println(
                "GET /shortlist"
        );

        server.start();
    }

    private static void health(
            HttpExchange exchange)
            throws IOException {

        if (!methodIs(exchange, "GET")) {
            send(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );
            return;
        }

        send(
                exchange,
                200,
                "{\"status\":\"ok\",\"service\":\"smart-resume-screener\"}"
        );
    }

    private static void screen(
            HttpExchange exchange)
            throws IOException {

        if (!methodIs(exchange, "GET")) {
            send(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );
            return;
        }

        try {

            List<CandidateResult> results =
                    runScreening();

            send(
                    exchange,
                    200,
                    candidatesToJson(results)
            );

        } catch (Exception e) {

            sendError(
                    exchange,
                    e
            );
        }
    }

    private static void shortlist(
            HttpExchange exchange)
            throws IOException {

        if (!methodIs(exchange, "GET")) {
            send(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );
            return;
        }

        try {

            List<CandidateResult> results =
                    runScreening();

            BatchScreener screener =
                    new BatchScreener();

            List<CandidateResult> topCandidates =
                    screener.shortlist(
                            results,
                            3
                    );

            send(
                    exchange,
                    200,
                    candidatesToJson(
                            topCandidates
                    )
            );

        } catch (Exception e) {

            sendError(
                    exchange,
                    e
            );
        }
    }

    private static List<CandidateResult>
    runScreening()
            throws Exception {

        if (!Files.exists(
                RESUME_DIRECTORY)) {

            throw new IOException(
                    "Resume directory not found: "
                            + RESUME_DIRECTORY
            );
        }

        if (!Files.exists(JOB_FILE)) {

            throw new IOException(
                    "Job description not found: "
                            + JOB_FILE
            );
        }

        String jobText =
                Files.readString(JOB_FILE);

        JobDescriptionParser parser =
                new JobDescriptionParser();

        JobDescription job =
                parser.parse(
                        "Software Engineering Position",
                        jobText
                );

        BatchScreener screener =
                new BatchScreener();

        return screener.screen(
                RESUME_DIRECTORY,
                job
        );
    }

    private static String candidatesToJson(
            List<CandidateResult> candidates) {

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"count\":");
        json.append(candidates.size());
        json.append(",");
        json.append("\"candidates\":[");

        for (int i = 0;
             i < candidates.size();
             i++) {

            CandidateResult candidate =
                    candidates.get(i);

            MatchResult match =
                    candidate.getMatchResult();

            if (i > 0) {
                json.append(",");
            }

            json.append("{");

            json.append("\"rank\":");
            json.append(i + 1);
            json.append(",");

            json.append("\"candidate\":\"");
            json.append(
                    escape(
                            candidate
                                    .getResume()
                                    .getName()
                    )
            );
            json.append("\",");

            json.append("\"file\":\"");
            json.append(
                    escape(
                            candidate
                                    .getResumeFile()
                    )
            );
            json.append("\",");

            json.append("\"score\":");
            json.append(
                    String.format(
                            "%.2f",
                            match.getScore()
                    )
            );
            json.append(",");

            json.append("\"matchedSkills\":[");
            appendStringArray(
                    json,
                    match.getMatchedSkills()
            );
            json.append("],");

            json.append("\"missingSkills\":[");
            appendStringArray(
                    json,
                    match.getMissingSkills()
            );
            json.append("],");

            json.append("\"justification\":\"");
            json.append(
                    escape(
                            match.getJustification()
                    )
            );
            json.append("\"");

            json.append("}");
        }

        json.append("]");
        json.append("}");

        return json.toString();
    }

    private static void appendStringArray(
            StringBuilder json,
            List<String> values) {

        for (int i = 0;
             i < values.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append("\"");
            json.append(
                    escape(values.get(i))
            );
            json.append("\"");
        }
    }

    private static boolean methodIs(
            HttpExchange exchange,
            String method) {

        return method.equalsIgnoreCase(
                exchange.getRequestMethod()
        );
    }

    private static String escape(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private static void sendError(
            HttpExchange exchange,
            Exception e)
            throws IOException {

        String message =
                e.getMessage();

        if (message == null) {
            message =
                    e.getClass()
                            .getSimpleName();
        }

        send(
                exchange,
                500,
                "{\"error\":\""
                        + escape(message)
                        + "\"}"
        );
    }

    private static void send(
            HttpExchange exchange,
            int status,
            String response)
            throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        try (
                var output =
                        exchange.getResponseBody()
        ) {

            output.write(bytes);
        }
    }
}
