---
name: fit-score-assessment
description: Score how well a job description matches a real, standard technical role on a 0-100 scale. Use when the model needs to produce the "fit_score" field for the job analysis.
---

# Fit Score Assessment

Calculate the fit score for the job analysis.

## Scale

| Score | Meaning |
|-------|---------|
| 90-100 | Clear, standard role description |
| 60-89 | Minor gaps or vague details |
| 30-59 | Vague or incomplete description |
| 0-29 | Not a job description at all |

## Rules

- Return a number between 0 and 100 (integer)
- Base the score on how complete, specific, and coherent the description is
- A description with a clear title, responsibilities, and requirements scores high
- Spam, unrelated content, or generic filler scores near 0
