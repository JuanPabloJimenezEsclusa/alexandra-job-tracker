---
name: company-research
description: Research a company's rating, type, and market position from its name. Use when the model needs to produce the "company_rating" and "company_type" fields for the job analysis.
---

# Company Research

Determine company rating and type for the job analysis.

## Definition

- `company_rating`: perceived company reputation on a 0.0-5.0 scale (Glassdoor-style)
- `company_type`: classification of the company

## Company Type Values

- `startup` — young, small, high-growth company (usually < 5 years, < 200 employees)
- `mid-size` — established company with a few hundred to a few thousand employees
- `enterprise` — large corporation (multi-national, well-known brand)
- `consulting` — agency or services company
- `government` — public sector, state-owned
- `nonprofit` — non-commercial organization
- `unknown` — cannot determine

## Rules

- Use your internal knowledge of well-known companies (e.g., FAANG → enterprise, seed-round startups → startup)
- For unknown or small companies, infer type from job description context (culture words, benefits, team size)
- Rating: use general market reputation; 3.0 is neutral, 4.0+ is well-regarded, 2.5- is poorly regarded
- Default to `unknown` type and `3.0` rating when there is no signal
- If the company is publicly known with public employee-review scores, use them
- Never invent a specific Glassdoor review count — only the rating
