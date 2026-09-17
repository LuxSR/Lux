-- Replace avg_points with per-game points and turn count in gamestats.
ALTER TABLE gamestats
    ADD COLUMN points INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN turn INTEGER NOT NULL DEFAULT 0;

-- Backfill existing seed rows:
--   501 game (game_id 1): winner reaches 0, avg_points approximates the scoring pace.
--   Cricket (game_id 2): points scored over the match.
--   301 game (game_id 3): winner reaches 0.
UPDATE gamestats SET points = 501, turn = 11 WHERE user_id = 1 AND game_id = 1;
UPDATE gamestats SET points = 501, turn = 12 WHERE user_id = 2 AND game_id = 1;
UPDATE gamestats SET points = 270, turn = 14 WHERE user_id = 1 AND game_id = 2;
UPDATE gamestats SET points = 301, turn = 7  WHERE user_id = 2 AND game_id = 3;

ALTER TABLE gamestats
    DROP COLUMN avg_points;