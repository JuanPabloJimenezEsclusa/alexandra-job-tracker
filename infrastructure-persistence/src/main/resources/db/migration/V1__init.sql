CREATE TABLE users
(
  id            UUID PRIMARY KEY,
  username      VARCHAR(100) UNIQUE      NOT NULL,
  password_hash VARCHAR(255)             NOT NULL,
  created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE applications
(
  id           UUID PRIMARY KEY,
  user_id      UUID                     NOT NULL REFERENCES users (id),
  company      VARCHAR(255)             NOT NULL,
  role         VARCHAR(255)             NOT NULL,
  source       VARCHAR(50)              NOT NULL,
  posting_url  TEXT,
  status       VARCHAR(50)              NOT NULL DEFAULT 'SAVED',
  date_applied TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  notes        TEXT
);

CREATE INDEX idx_applications_user_id ON applications (user_id);

CREATE TABLE job_postings
(
  id          UUID PRIMARY KEY,
  user_id     UUID         NOT NULL REFERENCES users (id),
  url         TEXT         NOT NULL,
  source      VARCHAR(50)  NOT NULL,
  title       VARCHAR(255) NOT NULL,
  company     VARCHAR(255) NOT NULL,
  description TEXT,
  posted_at   TIMESTAMP WITH TIME ZONE,
  UNIQUE (user_id, url)
);

CREATE INDEX idx_job_postings_user_id ON job_postings (user_id);
