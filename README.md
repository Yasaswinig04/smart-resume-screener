# Smart Resume Screener

## Overview

Smart Resume Screener is a Java-based application that automatically parses resumes, extracts structured candidate information, compares candidates against a job description, ranks candidates by suitability, stores screening data in SQLite, and exposes screening functionality through a REST API.

## Features

- PDF resume text extraction using Apache PDFBox
- Resume parsing and structured extraction
- Skill extraction using a configurable skill dictionary
- Job description parsing
- Candidate-to-job skill matching
- Match score from 0–10
- Matched and missing skill identification
- Candidate ranking and shortlisting
- SQLite database persistence
- REST API endpoints
- Optional LLM-based semantic matching
- Rule-based fallback when LLM access is unavailable

## Architecture

```text
Resume PDF/TXT
      |
      v
PdfResumeExtractor
      |
      v
ResumeParser
      |
      v
Structured Resume
      |
      +--------------------+
      |                    |
      v                    v
JobDescriptionParser   SQLite Database
      |
      v
JobDescription
      |
      v
JobMatcher / LLMMatcher
      |
      v
MatchResult
      |
      v
Candidate Ranking
      |
      v
Shortlist + Justification
      |
      v
REST API


Technology Stack
Java 21
Maven
Apache PDFBox 3.0.5
SQLite
SQLite JDBC
Java HTTP Server
Project Structure
smart-resume-screener/
├── database/
│   └── schema.sql
├── sample_data/
│   ├── job_description.txt
│   ├── resume1.txt
│   └── resumes/
│       └── yasaswini_resume.pdf
├── src/main/java/com/resume/screener/
│   ├── ApiServer.java
│   ├── BatchScreener.java
│   ├── CandidateResult.java
│   ├── DatabaseManager.java
│   ├── JobDescription.java
│   ├── JobDescriptionParser.java
│   ├── JobMatcher.java
│   ├── LLMMatcher.java
│   ├── LLMMatchResult.java
│   ├── Main.java
│   ├── MatchResult.java
│   ├── PdfResumeExtractor.java
│   ├── Resume.java
│   ├── ResumeParser.java
│   ├── ResumeRepository.java
│   └── ResumeScreener.java
├── pom.xml
└── README.md
How It Works
Resume PDFs are loaded from the sample data directory.
Apache PDFBox extracts text from each PDF.
ResumeParser extracts the candidate name, skills, education, experience, projects, certifications, leadership activities, and languages.
JobDescriptionParser extracts required skills from the job description.
JobMatcher performs rule-based skill matching.
When an OpenAI API key is available, LLMMatcher performs semantic matching and generates a 1–10 fit score with justification.
If LLM access is unavailable, the system uses the rule-based matcher as a fallback.
Candidates are ranked by their match score.
Resume, job description, and match results are stored in SQLite.
Results can also be accessed through the REST API.
LLM Prompt

The LLM is instructed to compare a candidate resume against a job description and produce a structured fit assessment.

Example prompt:

Compare the following resume with the following job description.

Resume:
{resume information}

Job Description:
{job description}

Evaluate the candidate based on:
- Required technical skills
- Relevant experience
- Education
- Projects
- Overall relevance to the position

Return:
SCORE: <number from 1 to 10>

JUSTIFICATION:
<concise explanation of the candidate's strengths, relevant experience, and skill gaps>

The score must be between 1 and 10 and the justification must explain the reasoning behind the score.
REST API

The application exposes screening functionality through a lightweight Java HTTP server.

Health Check
GET /health

Returns:

{
  "status": "ok"
}
Screen Candidates
GET /screen

Runs the resume screening workflow and returns candidate matching results.

Shortlisted Candidates
GET /shortlist

Returns the top-ranked candidates based on their match scores.

Database

The application uses SQLite to persist screening information.

The database stores:

Parsed resume information
Job descriptions
Candidate-to-job match results
Match scores

The database schema is provided in:

database/schema.sql

The SQLite database file is generated at runtime and is not required in the repository.

Running the Application
Prerequisites
Java 21
Maven
Compile
mvn clean compile
Run the command-line screener
mvn exec:java -Dexec.mainClass="com.resume.screener.Main"
Run the REST API
mvn exec:java -Dexec.mainClass="com.resume.screener.ApiServer"

The API runs on:

http://localhost:8080

Example:

curl http://localhost:8080/health
LLM Configuration

LLM-based semantic matching is optional.

When an OpenAI API key is configured through the environment, LLMMatcher can perform semantic resume-to-job matching and generate a score with justification.

The API key should be provided through the environment variable:

OPENAI_API_KEY

Do not commit the API key or .env files to the repository.

If the API key is unavailable, the application uses the rule-based matching implementation instead.

Example Output
=================================
       CANDIDATE RANKING
=================================

1. Candidate Name
   Score: 8.2/10
   Matched skills: [java, sql]
   Missing skills: [aws]
   Justification: Candidate has strong alignment with the
   required technical skills and relevant experience.

=================================
        TOP CANDIDATES
=================================
Design Notes

The system separates resume extraction, parsing, matching, persistence, and API responsibilities into dedicated classes.

This separation makes the application easier to maintain and allows the rule-based matcher and LLM-based matcher to be used as alternative matching strategies.

Limitations
Resume extraction quality depends on the structure and readability of the input document.
The rule-based matcher relies on the configured skill dictionary.
LLM matching requires a valid OpenAI API key and network access.
The current implementation is intended as a demonstration application rather than a production recruitment platform.
