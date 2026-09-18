import { Outlet } from 'react-router-dom';
import TopBar from './TopBar';
import SidebarMenu from './SidebarMenu';

export default function AppLayout() {
  return (
    <div className="app-layout">
      <TopBar />
      <div className="app-body">
        <SidebarMenu />
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
