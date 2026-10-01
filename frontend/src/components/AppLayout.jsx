import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import TopBar from './TopBar';
import SidebarMenu from './SidebarMenu';
import { useAuth } from '../useAuth';

export default function AppLayout() {
  const { isAuthenticated } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="app-layout">
      <TopBar
        sidebarOpen={sidebarOpen}
        onToggleSidebar={() => setSidebarOpen((isOpen) => !isOpen)}
      />
      <div className="app-body">
        {isAuthenticated && (
          <SidebarMenu
            isOpen={sidebarOpen}
            onClose={() => setSidebarOpen(false)}
          />
        )}
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
