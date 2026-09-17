import { useState } from 'react';
import { NavLink } from 'react-router-dom';

export default function SidebarMenu() {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside className={`sidebar ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <button
        className="btn sidebar-toggle"
        type="button"
        onClick={() => setCollapsed(!collapsed)}
      >
        {collapsed ? 'Expand' : 'Collapse'}
      </button>
      <nav className="sidebar-nav">
        <NavLink to="/sessions" className="sidebar-link">
          Sessions
        </NavLink>
        {/* TODO: change NavLink to profile */}
        <NavLink to="/" className="sidebar-link">
          Profile
        </NavLink>
      </nav>
    </aside>
  );
}
