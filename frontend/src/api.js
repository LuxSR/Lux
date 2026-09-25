import { jwtDecode } from 'jwt-decode';

const API_BASE_URL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

let unauthorizedHandler = null;

export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn;
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json' };
  if (auth && token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(`${API_BASE_URL}${path}`, {
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

export async function getFinishedGames({ sessionId }) {
  return request(`/api/session/${sessionId}/finished-games`);
}

function mapStats(stats) {
  return stats.map((s) => ({
    username: s.userName,
    points: s.points,
    turns: s.turns,
    bullseyes: s.bullseyes,
    triple20s: s.triple20s,
    highestScore: s.highestScore,
    highestCheckout: s.highestCheckout,
    checkoutAccuracy: s.checkoutAccuracy,
  }));
}

// Fetch current game state: per-player stats from the real stats endpoint;
// turn/isFinished/gametype from the most recent real GameResponse while
// playing, or from the session's finished-games list after a reload.
export async function getGameState({ sessionId, gameId }) {
  const id = Number(gameId);
  const stats = await request(`/api/session/${sessionId}/games/${id}/stats`);
  const meta = gameResponses[id] ?? {};

  if (meta.turn) {
    // Live path (played in this page session): GameResponse cache is warm.
    return {
      gameId: id,
      gametype: meta.gametype,
      isFinished: Boolean(meta.isFinished),
      winner: meta.isFinished ? meta.turn : null,
      turn: meta.turn,
      players: mapStats(stats),
    };
  }

  // Reload path: no cached GameResponse — derive finished/winner from the
  // session's finished-games list; turn = winner (finished) or the player
  // with the fewest turns (in-progress, best-effort).
  const finished = await getFinishedGames({ sessionId });
  const finishedGame = finished.find((f) => Number(f.gameId) === id);
  const minTurns = Math.min(...stats.map((s) => s.turns));
  const turn = stats.find((s) => s.turns === minTurns)?.userName;
  return {
    gameId: id,
    gametype: finishedGame?.gametype ?? meta.gametype,
    isFinished: Boolean(finishedGame),
    winner: finishedGame?.winner ?? null,
    turn: finishedGame?.winner ?? turn,
    players: mapStats(stats),
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
