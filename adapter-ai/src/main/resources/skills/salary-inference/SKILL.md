---
name: salary-inference
description: Infer a realistic salary range for the role from job title, seniority, company type, and location. Use when the model needs to produce the "salary_min", "salary_max", and "salary_currency" fields for the job analysis.
---

# Salary Inference

Infer a realistic salary range for the job analysis.

## Definition

- `salary_min`: lower bound of the annual gross salary range
- `salary_max`: upper bound of the annual gross salary range
- `salary_currency`: ISO 4217 currency code (default USD)

## Rules

- Base the range on the job title, seniority, and primary technology
- Adjust for company type: enterprise/consulting pays at or above market, startup varies (equity-heavy, base salary may be lower)
- Use global market benchmarks when the location is unknown; default to USD
- Seniority multipliers (relative to a mid-level baseline):
  - junior: 0.7-0.85x
  - mid: 1.0x (baseline)
  - senior: 1.2-1.5x
  - lead: 1.5-1.8x
- Keep the range realistic and internally consistent: `salary_max` must be >= `salary_min`
- If a salary is explicitly stated in the job description, use it as the anchor
- Never invent a salary when the role is not a job description — use 0.0 for both bounds
