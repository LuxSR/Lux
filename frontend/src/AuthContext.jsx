import { useEffect, useMemo, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { setUnauthorizedHandler } from './api';
import { AuthContext } from './context/AuthContext';

function getTokenExp(token) {
  try {
    return Number(jwtDecode(token).exp);
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
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
