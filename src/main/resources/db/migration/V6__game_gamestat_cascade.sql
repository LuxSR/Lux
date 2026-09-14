-- Cascade deletion of a Game to its GameStats rows.
ALTER TABLE gamestats
    DROP CONSTRAINT gamestats_game_id_fkey,
    ADD CONSTRAINT gamestats_game_id_fkey
        FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE;
