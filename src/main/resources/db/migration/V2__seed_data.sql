-- Seed data for dartgame database

-- Users
INSERT INTO users (user_name) VALUES
    ('Alice van Buren'),
    ('Bob de Groot'),
    ('Luke Littler')
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
INSERT INTO sessions (session_id, user_id) VALUES
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
INSERT INTO game_mm_gametypes (gameId, gametypeId) VALUES
    (1, 1),
    (2, 3),
    (3, 2),
    (4, 4)
ON CONFLICT DO NOTHING;

-- Playerstats
INSERT INTO playerstats (player_stat_id, avg_points, won_games, played_games, triple20s, bullseyes, highest_score, player_id) VALUES
    (1, 45.5, 2, 4, 12, 3, 180, 1),
    (2, 42.3, 1, 3, 8, 2, 140, 2),
    (3, 38.7, 0, 2, 5, 1, 120, 3)
ON CONFLICT DO NOTHING;

-- Gamestats
INSERT INTO gamestats (user_id, game_id, avg_points, triple20s, bullseyes, highest_score) VALUES
    (1, 1, 46.2, 4, 1, 180),
    (2, 1, 41.8, 2, 0, 120),
    (1, 2, 44.8, 3, 1, 160),
    (2, 3, 43.1, 3, 1, 140)
ON CONFLICT DO NOTHING;
