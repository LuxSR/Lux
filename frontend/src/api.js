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

export function createSession({ gamemodes }) {
  return request('/api/session', {
    method: 'POST',
    body: { games: gamemodes.map((gamemode) => ({ gameType: gamemode })) },
  });
}

function getUsername(token) {
  const payload = jwtDecode(token);
  return payload.sub;
}
