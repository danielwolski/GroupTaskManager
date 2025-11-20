-- docker exec -i -e PGPASSWORD=admin postgres psql -U admin -d grouptaskmanager < seed.sql

DROP TABLE IF EXISTS daily_task_archives CASCADE;
DROP TABLE IF EXISTS daily_tasks CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS groups CASCADE;

--  Tworzenie tabel
CREATE TABLE groups (
    group_id SERIAL PRIMARY KEY,
    passcode VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    login VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    group_id INT REFERENCES groups(group_id) ON DELETE SET NULL,
    user_role VARCHAR(50) NOT NULL
);

CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    group_id INT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    assignee_user_id INT REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE daily_tasks (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    current_day DATE DEFAULT CURRENT_DATE,
    group_id INT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    assignee_user_id INT REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE daily_task_archives (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    was_done BOOLEAN DEFAULT FALSE,
    task_date DATE DEFAULT CURRENT_DATE,
    archived_date DATE DEFAULT CURRENT_DATE,
    group_id INT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    created_by_user_id INT REFERENCES users(user_id) ON DELETE SET NULL
);

-- Dane
INSERT INTO groups (passcode)
VALUES
    ('alpha123'),
    ('bravo456');

INSERT INTO users (username, login, password, group_id, user_role)
VALUES
    ('Jan Kowalski', 'kowalski', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 1, 'USER'),
    ('Anna Nowak', 'nowak', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 1, 'USER'),
    ('Piotr Zieliński', 'zielinski', '$2a$10$iVUxI7XT..jDwnf/gYCC9Oj1JVZKUYBEAXErZmQFG/uV5UdkSeozG', 2, 'USER');

INSERT INTO tasks (description, done, group_id, assignee_user_id)
VALUES
    ('Przygotować raport tygodniowy', false, 1, 1),
    ('Spotkanie zespołu o 14:00', true, 1, 2),
    ('Utworzyć nową grupę testową', false, 2, 3);

INSERT INTO daily_tasks (description, done, group_id, assignee_user_id)
VALUES
    ('Wysłać status dnia', true, 1, 1),
    ('Uzupełnić listę uczestników', false, 1, 2),
    ('Sprawdzić backup systemu', true, 2, 3);

INSERT INTO daily_task_archives (description, was_done, group_id, created_by_user_id)
VALUES
    ('Zamknąć sprint 41', true, 1, 1),
    ('Zarchiwizować stare zadania', false, 1, 2),
    ('Sprawdzić błędy z testów integracyjnych', true, 2, 3);

SELECT 'Seed wczytany pomyślnie' AS status;
