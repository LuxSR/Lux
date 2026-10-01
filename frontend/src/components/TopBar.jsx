import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../useAuth';
import logo from '../assets/superdarterlogo.png';

export default function TopBar({ sidebarOpen, onToggleSidebar }) {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="topbar">
      {isAuthenticated && (
        <button
          className="btn topbar-menu-button"
          type="button"
          onClick={onToggleSidebar}
          aria-label={sidebarOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={sidebarOpen}
        >
          {sidebarOpen ? 'Close menu' : 'Menu'}
        </button>
      )}
      <NavLink to="/" className="topbar-logo">
        <img src={logo} alt="Lux logo" />
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
