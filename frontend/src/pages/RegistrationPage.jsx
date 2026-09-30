import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../useAuth';
import { register } from '../api';

export default function RegisterPage() {
  const [formData, setFormData] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  async function handleSubmit(e) {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setError('');
    try {
      const res = await register(formData);
      login(res.token);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page auth-page">
      <div className="card">
        <h1>Register</h1>
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
            <label>E-mail: </label>
            <input
              label="email"
              name="email"
              type="email"
              value={formData['email'] || ''}
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
          </div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Registering...' : 'Register'}
          </button>
        </form>
        {error && <p className="error-message">{error}</p>}
        <div className="form-footer">
          Already have an account? <Link to="/login">Login</Link>
        </div>
      </div>
    </div>
  );
}
