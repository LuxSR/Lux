import { useParams } from 'react-router-dom';
import { Link } from 'react-router-dom';

export default function GamePage() {
  const { id, gameId } = useParams();

  return (
    <div className="page game-page">
      <p className="game-page-placeholder">
        Game {gameId} — coming soon
      </p>
      <Link className="btn session-detail-back" to={`/session/${id}`}>
        &larr; Back to Session
      </Link>
    </div>
  );
}