import { Navigate, Route, Routes } from 'react-router-dom';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './AuthContext.jsx';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegistrationPage';
import HomePage from './pages/HomePage';
import SessionsPage from './pages/SessionsPage';
import SessionDetailPage from './pages/SessionDetailPage';
import GamePage from './pages/GamePage';
import AppLayout from './components/AppLayout';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<AppLayout />}>
            {/* Pages with topbar+sidemenu, log in not required */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              {/* Pages with topbar+sidemenu, log in required*/}
              <Route path="/sessions" element={<SessionsPage />} />
              <Route path="/session/:id" element={<SessionDetailPage />} />
              <Route
                path="/session/:id/game/:gameId"
                element={<GamePage />}
              />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
