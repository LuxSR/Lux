-- 1. Add the column (default 0 so existing rows stay valid).
ALTER TABLE gamestats ADD COLUMN position INTEGER NOT NULL DEFAULT 0;

-- 2. Backfill seed rows deterministically (player order per game, 0-based).
UPDATE gamestats g
SET position = sub.rn - 1
FROM (
         SELECT user_id, game_id,
                ROW_NUMBER() OVER (PARTITION BY game_id ORDER BY user_id) AS rn
         FROM gamestats
     ) sub
WHERE g.user_id = sub.user_id AND g.game_id = sub.game_id;

-- 3. Enforce one position per game.
ALTER TABLE gamestats
    ADD CONSTRAINT gamestats_position_unique UNIQUE (game_id, position);