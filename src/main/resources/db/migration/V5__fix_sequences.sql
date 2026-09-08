-- Fix identity sequences desynced by V2 seed data.
-- V2 inserted rows with explicit ids (sessions, games, playerstats) but the
-- BIGSERIAL sequences were never advanced past their initial value, so the next
-- generated id collided with an existing row (duplicate key on ..._pkey).
-- Reset each sequence to the highest existing id so the next value is max + 1.

SELECT setval('sessions_session_id_seq',              (SELECT COALESCE(MAX(session_id), 1)      FROM sessions),     true);
SELECT setval('games_game_id_seq',                    (SELECT COALESCE(MAX(game_id), 1)        FROM games),       true);
SELECT setval('playerstats_player_stat_id_seq',       (SELECT COALESCE(MAX(player_stat_id), 1) FROM playerstats), true);
