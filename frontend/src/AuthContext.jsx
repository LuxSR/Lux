import { useEffect, useMemo, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { setUnauthorizedHandler } from './api';
import { AuthContext } from './context/AuthContext';

const ADMIN_ROLE = 'ADMIN';

function getTokenExp(token) {
  try {
    return Number(jwtDecode(token).exp);
  } catch {
    return null;
  }
}

function getTokenRole(token) {
  try {
    return jwtDecode(token).role ?? null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  const isAuthenticated = useMemo(() => {
    if (!token) return false;
    const exp = getTokenExp(token);
    return exp != null && exp > Date.now() / 1000;
  }, [token]);

  // UX gate only. jwtDecode does not verify the signature, so the backend's own
  // check remains the real enforcement.
  const isAdmin = useMemo(
    () => token != null && getTokenRole(token) === ADMIN_ROLE,
    [token]
  );

  function login(newToken) {
    localStorage.setItem('token', newToken);
    setToken(newToken);
  }

  function logout() {
    localStorage.removeItem('token');
    setToken(null);
  }

  useEffect(() => {
    setUnauthorizedHandler(logout);
  }, []);

  return (
    <AuthContext.Provider value={{ isAuthenticated, isAdmin, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
