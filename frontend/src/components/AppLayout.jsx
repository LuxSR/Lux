import { Outlet } from 'react-router-dom';
import TopBar from './TopBar';
import SidebarMenu from './SidebarMenu';
import { useAuth } from '../useAuth';

export default function AppLayout() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="app-layout">
      <TopBar />
      <div className="app-body">
        {isAuthenticated && <SidebarMenu />}
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
