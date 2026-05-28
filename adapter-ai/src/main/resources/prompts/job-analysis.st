Analyze the job description below and return ONLY valid JSON (no markdown, no code fences).

Schema fields:
- summary: max 20 words describing role + seniority + primary technology
- seniority: one of junior, mid, senior, lead, or unknown
- technical_skills: array of explicitly mentioned tools, languages, frameworks
- soft_skills: array of explicitly mentioned interpersonal skills
- fit_score: number 0-100

Rules:
- fit_score: 90-100 clear standard role, 60-89 minor gaps, 30-59 vague, 0-29 not a job description
- Only extract explicitly stated skills — do not infer
- seniority: derive from explicit labels or years of experience; "unknown" if ambiguous
- Never omit keys — use null for strings, empty array for lists

JOB DESCRIPTION:
{description}