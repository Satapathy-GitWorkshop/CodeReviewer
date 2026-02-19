-- Initial schema for Code Review Dashboard

CREATE TABLE repositories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(512) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    last_analyzed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    default_branch VARCHAR(100) DEFAULT 'main',
    language VARCHAR(100)
);

CREATE TABLE commits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    sha VARCHAR(40) NOT NULL UNIQUE,
    message TEXT,
    author VARCHAR(255),
    author_email VARCHAR(255),
    commit_date TIMESTAMP,
    html_url VARCHAR(1024),
    files_changed INT,
    additions INT,
    deletions INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_commit_repo FOREIGN KEY (repository_id) REFERENCES repositories(id)
);

CREATE TABLE analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    commit_sha VARCHAR(40) NOT NULL,
    language VARCHAR(100),
    tool_name VARCHAR(100),
    issues TEXT,
    issue_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_repo FOREIGN KEY (repository_id) REFERENCES repositories(id)
);

CREATE TABLE ai_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    commit_sha VARCHAR(40) NOT NULL UNIQUE,
    summary TEXT,
    risk_score INT,
    risk_level VARCHAR(20),
    improvements TEXT,
    security_concerns TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_repo FOREIGN KEY (repository_id) REFERENCES repositories(id)
);

CREATE INDEX idx_commits_repo_date ON commits(repository_id, commit_date DESC);
CREATE INDEX idx_analysis_commit ON analysis_results(commit_sha);
CREATE INDEX idx_ai_commit ON ai_summaries(commit_sha);
