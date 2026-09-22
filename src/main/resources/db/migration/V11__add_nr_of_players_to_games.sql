-- Add player count per game.
ALTER TABLE games ADD COLUMN nr_of_players INTEGER NOT NULL DEFAULT 0;

-- Backfill from gamestats (one row per participating player).
UPDATE games g
SET nr_of_players = sub.cnt
FROM (
    SELECT game_id, COUNT(*) AS cnt
    FROM gamestats
    GROUP BY game_id
) sub
WHERE g.game_id = sub.game_id;