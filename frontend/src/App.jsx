import { Navigate, Route, Routes } from 'react-router-dom';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './AuthContext.jsx';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegistrationPage';
import HomePage from './pages/HomePage';
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          {/* <Route path="/home-page" element={<ProtectedRoute><SolarWatchPage /></ProtectedRoute>} /> */}
          <Route path="/" element={<HomePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
