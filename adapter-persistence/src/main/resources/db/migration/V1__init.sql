CREATE TABLE users
(
  id            UUID PRIMARY KEY,
  username      VARCHAR(100) UNIQUE      NOT NULL,
  password_hash VARCHAR(255)             NOT NULL,
  created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE job_postings
(
  id          UUID PRIMARY KEY,
  user_id     UUID         NOT NULL REFERENCES users (id),
  url         TEXT         NOT NULL,
  source      VARCHAR(50)  NOT NULL,
  title       TEXT         NOT NULL,
  company     TEXT         NOT NULL,
  description TEXT,
  posted_at   TIMESTAMP WITH TIME ZONE,
  UNIQUE (user_id, url)
);

CREATE INDEX idx_job_postings_user_id ON job_postings (user_id);

CREATE TABLE applications
(
  id           UUID PRIMARY KEY,
  user_id      UUID                     NOT NULL REFERENCES users (id),
  company      TEXT                     NOT NULL,
  role         VARCHAR(255)             NOT NULL,
  source       VARCHAR(50)              NOT NULL,
  posting_url  TEXT,
  status       VARCHAR(50)              NOT NULL DEFAULT 'SAVED',
  date_applied TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  notes        TEXT,
  version      BIGINT                   NOT NULL DEFAULT 0
);

CREATE INDEX idx_applications_user_id ON applications (user_id);

CREATE TABLE job_analyses
(
  id               UUID PRIMARY KEY,
  job_posting_id   UUID                     NOT NULL REFERENCES job_postings (id),
  user_id          UUID                     NOT NULL REFERENCES users (id),
  summary          TEXT                     NOT NULL,
  seniority        VARCHAR(50)              NOT NULL,
  soft_skills      TEXT                     NOT NULL DEFAULT '',
  technical_skills TEXT                     NOT NULL DEFAULT '',
  fit_score        DOUBLE PRECISION         NOT NULL,
  company_rating   DOUBLE PRECISION         NOT NULL DEFAULT 0,
  company_type     VARCHAR(50)              NOT NULL DEFAULT '',
  salary_min       DOUBLE PRECISION         NOT NULL DEFAULT 0,
  salary_max       DOUBLE PRECISION         NOT NULL DEFAULT 0,
  salary_currency  VARCHAR(10)              NOT NULL DEFAULT 'USD',
  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE (job_posting_id)
);

CREATE INDEX idx_job_analyses_user_id ON job_analyses (user_id);
