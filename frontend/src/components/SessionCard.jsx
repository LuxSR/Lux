import { useNavigate } from 'react-router-dom';

export default function SessionCard({ session }) {
  const navigate = useNavigate();

  return (
    <button
      type="button"
      className={`card session-card${session.isActive ? ' session-card-active' : ''}`}
      onClick={() => navigate(`/session/${session.id}`)}
    >
      <div className="session-card-header">
        <span className="session-card-date">
          {new Date(session.date).toLocaleDateString()}
        </span>
        {session.isActive && <span className="session-card-badge">Active</span>}
      </div>
      <p className="session-card-gamemodes">
        {session.gametypes.map((gametype) => gametype.gamemode).join(', ')}
      </p>
    </button>
  );
}
