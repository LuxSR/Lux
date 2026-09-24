import { jwtDecode } from 'jwt-decode';

let unauthorizedHandler = null;

export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn;
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json' };
  if (auth && token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(`${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 401) {
    unauthorizedHandler?.();
    throw new Error('Unauthorized');
  }
  if (!res.ok) {
    const message = await res.text().catch(() => '');
    const err = new Error(message || `Request failed: ${res.status}`);
    err.status = res.status;
    throw err;
  }
  return res.json();
}

export function register({ username, password, email }) {
  return request('/api/auth/register', {
    method: 'POST',
    body: { username, password, email },
    auth: false,
  });
}

export function login({ username, password }) {
  return request('/api/auth/login', {
    method: 'POST',
    body: { username, password },
    auth: false,
  });
}

export function getSessions() {
  const token = localStorage.getItem('token');
  const username = getUsername(token);
  return request(`/api/session?username=${encodeURIComponent(username)}`);
}

export function getAllGamemodes() {
  return request('/api/gamemode');
}

export function getSessionById(id) {
  return request(`/api/session/${id}`);
}

// Latest real GameResponse per game (turn/isFinished/gametype), written ONLY from
// real backend responses (startGame/playRound). Resets on page reload.
const gameResponses = {};

export async function startGame({ sessionId, gameType }) {
  const res = await request(`/api/session/${sessionId}/games?gametype=${gameType}`);
  gameResponses[res.gameId] = res;
  return res;
}

export async function createSession({ gamemodes, players = [] }) {
  return request('/api/session', {
    method: 'POST',
    body: {
      games: gamemodes.map((gamemode) => ({ gameType: gamemode })),
      players: players.map((username) => ({ username })),
    },
  });
}

export const MAX_TOTAL_PLAYERS = 10;

// Fetch current game state: players + points from the real stats endpoint,
// turn/isFinished/gametype from the most recent real GameResponse.
export async function getGameState({ sessionId, gameId }) {
  const id = Number(gameId);
  const stats = await request(`/api/session/${sessionId}/games/${id}/stats`);
  const meta = gameResponses[id] ?? {};

  let turn = meta.turn;
  if (!turn) {
    // Reload mid-game: no GameResponse cached — best-effort from stats
    // (the player with the fewest turns is up next).
    const minTurns = Math.min(...stats.map((s) => s.turns));
    turn = stats.find((s) => s.turns === minTurns)?.userName;
  }

  const isFinished = Boolean(meta.isFinished);
  return {
    gameId: id,
    gametype: meta.gametype,
    isFinished,
    winner: isFinished ? meta.turn : null,
    turn,
    players: stats.map((s) => ({ username: s.userName, points: s.points })),
  };
}

// Submit a round of 3 darts — REAL backend call. The response carries the next
// turn / finished state; the scoreboard is refreshed via getGameState after.
export async function playRound({ sessionId, gameId, username, score }) {
  const res = await request(`/api/session/${sessionId}/games`, {
    method: 'PUT',
    body: { username, score, gameId },
  });
  gameResponses[res.gameId] = res;
  return res;
}

export function getUsername(token) {
  const payload = jwtDecode(token);
  return payload.sub;
}
