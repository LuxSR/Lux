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
    const err = new Error(`Request failed: ${res.status}`);
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

export function startGame({ sessionId, gameType }) {
  return request(
    `/api/session/${sessionId}/games?gametype=${gameType}`
  );
}

export function createSession({ gamemodes }) {
  return request('/api/session', {
    method: 'POST',
    body: { games: gamemodes.map((gamemode) => ({ gameType: gamemode })) },
  });
}

// TEMP mock store for game state (resets on page reload)
const mockGames = {};
const MOCK_TARGET = { '301': 301, '501': 501 };

function ensureMockGame({ sessionId, gameId }) {
  const username = getUsername(localStorage.getItem('token'));
  if (!mockGames[gameId]) {
    mockGames[gameId] = {
      sessionId,
      gametype: '301',
      gameId: Number(gameId),
      isFinished: false,
      winner: null,
      turn: username,
      players: [{ username, points: 0 }],
    };
  }
  return mockGames[gameId];
}

// Parse a round score string ("20 3 15 1") into a total, mirroring
// GameService.getScoreFromRound. null for an invalid score.
function scoreStringTotal(score) {
  const numbers = String(score).match(/\d+/g)?.map(Number) ?? [];
  if (numbers.length === 0) return 0; // all misses
  if (numbers.length % 2 !== 0) return null;
  let total = 0;
  for (let i = 0; i < numbers.length / 2; i++) {
    const number = numbers[2 * i];
    const multiplier = numbers[2 * i + 1];
    const isBullseye = number === 25;
    const valid = isBullseye
      ? multiplier === 1 || multiplier === 2
      : number >= 1 && number <= 20 && multiplier >= 1 && multiplier <= 3;
    if (!valid) return null;
    total += number * multiplier;
  }
  return total;
}

// Apply one round to the mock display store, mirroring GameService.playGame:
// points accumulate; hitting the exact target wins; exceeding it is a bust.
function applyRoundToMock(game, username, score, gameResponse) {
  game.turn = gameResponse.turn;
  game.isFinished = gameResponse.isFinished;
  game.winner = gameResponse.isFinished ? gameResponse.turn : null;
  const player = game.players.find((p) => p.username === username);
  if (!player) return;
  const result = scoreStringTotal(score);
  if (result === null) return;
  const target = MOCK_TARGET[game.gametype] ?? 301;
  if (player.points + result < target) {
    player.points += result;
  } else if (player.points + result === target) {
    player.points = target;
  }
}

// Fetch current game state (scoreboard).
// FINAL: return request(`/api/session/${sessionId}/games/${gameId}`);
export async function getGameState({ sessionId, gameId }) {
  const game = ensureMockGame({ sessionId, gameId });
  return { ...game, players: game.players.map((p) => ({ ...p })) };
}

// Submit a round of 3 darts — REAL backend call (implemented, not mocked).
export async function playRound({ sessionId, gameId, username, score }) {
  const res = await request(`/api/session/${sessionId}/games`, {
    method: 'PUT',
    body: { username, score, gameId },
  });
  // Sync the (mocked, display-only) scoreboard with the real result.
  applyRoundToMock(ensureMockGame({ sessionId, gameId }), username, score, res);
  return res;
}

export function getUsername(token) {
  const payload = jwtDecode(token);
  return payload.sub;
}
