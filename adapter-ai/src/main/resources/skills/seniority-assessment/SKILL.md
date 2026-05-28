---
name: seniority-assessment
description: Determine the seniority level of a role from a job description. Use when the model needs to produce the "seniority" field for the job analysis.
---

# Seniority Assessment

Determine the seniority level for the job analysis.

## Allowed Values

- `junior`
- `mid`
- `senior`
- `lead`
- `unknown`

## Rules

- Derive seniority from explicit labels (e.g., "Junior Developer", "Senior Engineer", "Lead Architect")
- If no explicit label, use stated years of experience: 0-2 → junior, 2-5 → mid, 5+ → senior
- Leadership or team-management responsibilities imply `lead`
- Return `unknown` when the description is ambiguous or gives no signal
- Never guess when there is no evidence
