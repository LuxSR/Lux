import { useNavigate } from 'react-router-dom';

export default function FinishedGamesList({ games, sessionId }) {
  const navigate = useNavigate();

  if (games.length === 0) {
    return (
      <p className="state-message session-detail-empty">
        No finished games yet
      </p>
    );
  }

  return (
    <div className="session-detail-finished">
      {games.map((game) => (
        <div key={game.gameId} className="card finished-game-card">
          <span className="finished-game-label">
            {game.gametype} &middot; Game #{game.gameId}
          </span>
          <span className="finished-game-winner">Winner: {game.winner}</span>
          <button
            type="button"
            className="btn btn-primary finished-game-view"
            onClick={() => navigate(`/session/${sessionId}/game/${game.gameId}`)}
          >
            View
          </button>
        </div>
      ))}
    </div>
  );
}