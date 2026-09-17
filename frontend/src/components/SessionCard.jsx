import { useNavigate } from 'react-router-dom';

export default function SessionCard({ session }) {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      className={`card session-card${session.active ? ' session-card-active' : ''}`}
      onClick={() => navigate(`/session/${session.sessionId}`)}
    >
      <div className="session-card-header">
        <span className="session-card-date">
          {new Date(session.date).toLocaleDateString()}
        </span>
        {session.active && <span className="session-card-badge">Active</span>}
      </div>
      <p className="session-card-gamemodes">{session.gamemodes.join(', ')}</p>
    </button>
  );
}
