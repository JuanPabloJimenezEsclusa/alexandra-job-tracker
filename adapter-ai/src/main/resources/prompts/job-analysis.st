Analyze the job description below and return ONLY valid JSON (no markdown, no code fences).

Invoke the available skills to apply professional analysis before responding:
- job-summary: write the summary field
- seniority-assessment: determine the seniority field
- technical-skills-extraction: fill the technical_skills field
- soft-skills-extraction: fill the soft_skills field
- fit-score-assessment: compute the fit_score field
- company-research: compute the company_rating and company_type fields
- salary-inference: compute the salary_min, salary_max, and salary_currency fields

Schema fields:
- summary: max 20 words describing role + seniority + primary technology
- seniority: one of junior, mid, senior, lead, or unknown
- technical_skills: array of explicitly mentioned tools, languages, frameworks
- soft_skills: array of explicitly mentioned interpersonal skills
- fit_score: number 0-100
- company_rating: number 0.0-5.0 (Glassdoor-style)
- company_type: one of startup, mid-size, enterprise, consulting, government, nonprofit, or unknown
- salary_min: number (annual gross salary lower bound)
- salary_max: number (annual gross salary upper bound)
- salary_currency: ISO 4217 currency code (default USD)

Rules:
- Only extract explicitly stated skills — do not infer
- Never omit keys — use null for strings, empty array for lists, 0.0 for unknown numbers

JOB DESCRIPTION:
{description}
