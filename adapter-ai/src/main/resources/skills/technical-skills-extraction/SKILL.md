---
name: technical-skills-extraction
description: Extract the technical skills explicitly mentioned in a job description. Use when the model needs to produce the "technical_skills" field for the job analysis.
---

# Technical Skills Extraction

Extract the technical skills for the job analysis.

## Definition

Technical skills are explicitly mentioned tools, programming languages, frameworks, platforms, and technologies.

## Rules

- Extract only explicitly stated skills — never infer or guess
- Include languages, frameworks, databases, cloud platforms, and tools
- Preserve the exact name as written (e.g., "Spring Boot", "AWS", "PostgreSQL")
- Skip generic phrases such as "modern technologies" or "industry best practices"
- Return an empty array if no technical skill is explicitly mentioned
