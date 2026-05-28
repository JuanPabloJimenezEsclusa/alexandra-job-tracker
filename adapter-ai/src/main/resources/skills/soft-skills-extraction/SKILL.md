---
name: soft-skills-extraction
description: Extract the soft skills explicitly mentioned in a job description. Use when the model needs to produce the "soft_skills" field for the job analysis.
---

# Soft Skills Extraction

Extract the soft skills for the job analysis.

## Definition

Soft skills are explicitly mentioned interpersonal and behavioral abilities, such as communication, teamwork, leadership, and problem-solving.

## Rules

- Extract only explicitly stated skills — never infer or guess
- Include interpersonal, communication, collaboration, and behavioral traits
- Preserve the exact name as written (e.g., "Communication", "Teamwork")
- Do not include technical skills here
- Return an empty array if no soft skill is explicitly mentioned
