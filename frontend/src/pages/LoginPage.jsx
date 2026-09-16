import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../useAuth';
import { login } from '../api';

export default function LoginPage() {
  const [formData, setFormData] = useState({});
  const [error, setError] = useState('');
  const { login: authLogin } = useAuth();
  const navigate = useNavigate();

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      const res = await login(formData);
      authLogin(res.token);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page auth-page">
      <div className="card">
        <h1>Login</h1>
        <form className="form" onSubmit={handleSubmit}>
          <div>
            <label>Username: </label>
            <input
              label="Username"
              name="username"
              value={formData['username'] || ''}
              onChange={handleChange}
            />
          </div>
          <div>
            <label>Password: </label>
            <input
              label="Password"
              name="password"
              type="password"
              value={formData['password'] || ''}
              onChange={handleChange}
            />
          </div>{' '}
          <button className="btn btn-primary" type="submit">
            Login
          </button>
        </form>
        {error && <p className="error-message">{error}</p>}
        <div className="form-footer">
          New here? <Link to="/register">Register an account!</Link>
        </div>
      </div>
    </div>
  );
}
