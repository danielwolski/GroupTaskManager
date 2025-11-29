-- Seed data for Auth Service Database (authdb)
-- Run: docker exec -i -e PGPASSWORD=admin auth-db psql -U admin -d authdb < seed-auth.sql

-- Groups
INSERT INTO groups (passcode) VALUES ('alpha123') ON CONFLICT (passcode) DO NOTHING;
INSERT INTO groups (passcode) VALUES ('bravo456') ON CONFLICT (passcode) DO NOTHING;

-- Users (password: password123)
INSERT INTO users (username, login, password, group_id, user_role)
SELECT 'Jan Kowalski', 'kowalski@test.pl', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 
       (SELECT id FROM groups WHERE passcode = 'alpha123'), 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'kowalski@test.pl');

INSERT INTO users (username, login, password, group_id, user_role)
SELECT 'Anna Nowak', 'nowak@test.pl', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 
       (SELECT id FROM groups WHERE passcode = 'alpha123'), 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'nowak@test.pl');

INSERT INTO users (username, login, password, group_id, user_role)
SELECT 'Piotr Zieliński', 'zielinski@test.pl', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 
       (SELECT id FROM groups WHERE passcode = 'bravo456'), 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'zielinski@test.pl');

SELECT 'Auth DB seed completed' AS status;

