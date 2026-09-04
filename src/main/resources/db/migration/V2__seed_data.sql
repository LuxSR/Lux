-- Seed data for dartgame database

-- Roles
INSERT INTO roles (role) VALUES
    ('USER'),
    ('ADMIN')
ON CONFLICT DO NOTHING;

-- Users (passwords are BCrypt hashes of alice-password / bob-password / luke-password)
INSERT INTO users (user_name, password, role_id) VALUES
    ('Alice van Buren', '$2a$10$90H0tlpiij.zBSIhZ0WIWO8JsVix.7s/qpaCQ8JkAarKFvzmwvt4e', 1),
    ('Bob de Groot', '$2a$10$7wugsgxqkOm9DnccTqLAC.yBpoXgcpmDJ.YVmkKFESXNBmqkFT94K', 1),
    ('Luke Littler', '$2a$10$ttow8mJ9rIeFtNzij286Penfx.eJL/tml.1mUqxcQnq12IFk3INI.', 1)
ON CONFLICT DO NOTHING;

-- Gametypes (TAGs)
INSERT INTO gametypes (gametype) VALUES
    ('501'),
    ('301'),
    ('Cricket'),
    ('Killer'),
    ('Around the Clock')
ON CONFLICT DO NOTHING;

-- Sessions
INSERT INTO sessions (session_id, is_active, owner_id) VALUES
    (1, TRUE, 1),
    (2, TRUE, 2)
ON CONFLICT DO NOTHING;

-- Session players (owner is always among the players)
INSERT INTO session_mm_users (session_id, user_id) VALUES
    (1, 1),
    (2, 2)
ON CONFLICT DO NOTHING;

-- Games
INSERT INTO games (game_id, played_at, session_id, winner_id) VALUES
    (1, '2026-09-01 19:30:00', 1, 1),
    (2, '2026-09-01 20:15:00', 1, 1),
    (3, '2026-09-01 19:45:00', 2, 2),
    (4, '2026-09-01 20:30:00', 2, 2)
ON CONFLICT DO NOTHING;

-- Game-Gametype associations
INSERT INTO game_mm_gametypes (game_id, gametype_id) VALUES
    (1, 1),
    (2, 3),
    (3, 2),
    (4, 4)
ON CONFLICT DO NOTHING;

-- Playerstats
INSERT INTO playerstats (player_stat_id, avg_points, won_games, played_games, triple20s, bullseyes, highest_score, highest_checkout, avg_checkout_accuracy, player_id) VALUES
    (1, 45.5, 2, 4, 12, 3, 180, 110, 32.5, 1),
    (2, 42.3, 1, 3, 8, 2, 140, 81, 28.1, 2),
    (3, 38.7, 0, 2, 5, 1, 120, 60, 21.4, 3)
ON CONFLICT DO NOTHING;

-- Gamestats
INSERT INTO gamestats (user_id, game_id, avg_points, triple20s, bullseyes, highest_score, checkout_accuracy, highest_checkout) VALUES
    (1, 1, 46.2, 4, 1, 180, 30.0, 110),
    (2, 1, 41.8, 2, 0, 120, 0.0, 0),
    (1, 2, 44.8, 3, 1, 160, 25.0, 81),
    (2, 3, 43.1, 3, 1, 140, 20.0, 60)
ON CONFLICT DO NOTHING;
