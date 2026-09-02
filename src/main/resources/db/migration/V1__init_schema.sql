DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS gametypes;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS game_mm_gametypes;
DROP TABLE IF EXISTS playerstats;
DROP TABLE IF EXISTS gamestats;

CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL
);

CREATE TABLE gametypes (
    gametype_id BIGSERIAL PRIMARY KEY,
    gametype VARCHAR(255) NOT NULL
);

CREATE TABLE sessions (
    session_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
);

CREATE TABLE games (
    game_id BIGSERIAL PRIMARY KEY,
    played_at TIMESTAMP,
    session_id BIGINT NOT NULL REFERENCES sessions(session_id),
    winner_id BIGINT REFERENCES users(user_id)
);

CREATE TABLE game_mm_gametypes (
    gameId BIGINT NOT NULL REFERENCES games(game_id),
    gametypeId BIGINT NOT NULL REFERENCES gametypes(gametype_id),
    PRIMARY KEY (gameId, gametypeId)
);

CREATE TABLE playerstats (
    player_stat_id BIGSERIAL PRIMARY KEY,
    avg_points REAL,
    won_games INTEGER,
    played_games INTEGER,
    triple20s INTEGER,
    bullseyes INTEGER,
    highest_score INTEGER,
    player_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id)
);

CREATE TABLE gamestats (
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    game_id BIGINT NOT NULL REFERENCES games(game_id),
    avg_points REAL,
    triple20s INTEGER,
    bullseyes INTEGER,
    highest_score INTEGER,
    PRIMARY KEY (user_id, game_id)
);
