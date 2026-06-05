Analyze the job description below and return ONLY valid JSON (no markdown, no code fences).

Schema:
{
  "summary": "<max 20 words: role + seniority + primary technology>",
  "seniority": "<junior | mid | senior | lead | unknown>",
  "technical_skills": ["<explicitly mentioned tools, languages, frameworks>"],
  "soft_skills": ["<explicitly mentioned interpersonal skills>"],
  "fit_score": 0.0
}

Rules:
- fit_score: 90-100 clear standard role, 60-89 minor gaps, 30-59 vague, 0-29 not a job description
- Only extract explicitly stated skills — do not infer
- seniority: derive from explicit labels or years of experience; "unknown" if ambiguous
- Never omit keys — use null for strings, [] for arrays

JOB DESCRIPTION:
{description}
