import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../useAuth';
import logo from '../assets/superdarterlogo.png';

export default function TopBar() {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="topbar">
      <NavLink to="/" className="topbar-logo">
        <img src={logo} alt="Lux logo" />
        <span>Lux</span>
      </NavLink>
      {isAuthenticated ? (
        <button
          className="btn topbar-logout"
          type="button"
          onClick={handleLogout}
        >
          Logout
        </button>
      ) : (
        <NavLink className="btn topbar-login" to="/login">
          Login
        </NavLink>
      )}
    </header>
  );
}
