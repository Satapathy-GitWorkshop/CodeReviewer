-- Sample data for development/testing
-- This inserts a sample repository entry so the dashboard has something to show

INSERT INTO repositories (owner, name, full_name, is_active, language, default_branch, created_at)
VALUES 
    ('torvalds', 'linux', 'torvalds/linux', TRUE, 'C', 'master', CURRENT_TIMESTAMP),
    ('facebook', 'react', 'facebook/react', TRUE, 'JavaScript', 'main', CURRENT_TIMESTAMP);
