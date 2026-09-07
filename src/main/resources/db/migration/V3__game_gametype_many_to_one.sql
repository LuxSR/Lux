-- Refactor Game-Gametype from ManyToMany to ManyToOne.
-- Adds a gametype_id FK to games, migrates the existing join-table data,
-- drops the join table, and updates seed data to match.

-- 1. Add the new column (nullable initially so we can backfill).
ALTER TABLE games ADD COLUMN gametype_id BIGINT;

-- 2. Backfill gametype_id from the old join table.
UPDATE games g
SET gametype_id = jt.gametype_id
FROM game_mm_gametypes jt
WHERE jt.game_id = g.game_id;

-- 3. Enforce NOT NULL and the foreign key. This fails if any game has no gametype,
--    which would indicate incomplete join-table data.
ALTER TABLE games ALTER COLUMN gametype_id SET NOT NULL;
ALTER TABLE games ADD CONSTRAINT fk_games_gametype
    FOREIGN KEY (gametype_id) REFERENCES gametypes(gametype_id);

-- 4. Drop the join table.
DROP TABLE IF EXISTS game_mm_gametypes;

-- 5. Update seed data: the previously seeded games already carry gametype_id from
--    the backfill, but ensure the missing seed-game associations are set.
UPDATE games SET gametype_id = 1 WHERE game_id = 1 AND gametype_id IS NULL;
UPDATE games SET gametype_id = 3 WHERE game_id = 2 AND gametype_id IS NULL;
UPDATE games SET gametype_id = 2 WHERE game_id = 3 AND gametype_id IS NULL;
UPDATE games SET gametype_id = 4 WHERE game_id = 4 AND gametype_id IS NULL;
