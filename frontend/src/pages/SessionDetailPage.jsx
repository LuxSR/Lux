import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getFinishedGames, getSessionById, getSessions, startGame } from '../api';
import GameTypeCard from '../components/GameTypeCard';
import FinishedGamesList from '../components/FinishedGamesList';
import AddGamesModal from '../components/AddGamesModal';
import ConfirmFinishModal from '../components/ConfirmFinishModal';

// &larr is an HTML entity for a left-pointing arrow (←)
const BackToSessions = (
  <Link className="btn session-detail-back" to="/sessions">
    &larr; Back to Sessions
  </Link>
);

export default function SessionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [session, setSession] = useState(null);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [finished, setFinished] = useState({});
  const [finishedGames, setFinishedGames] = useState([]);
  const [addingGames, setAddingGames] = useState(false);
  const [finishingSession, setFinishingSession] = useState(false);
  // null until the ownership lookup resolves.
  const [isOwner, setIsOwner] = useState(null);

  useEffect(() => {
    let cancelled = false;
    getSessionById(id)
      .then((data) => {
        if (!cancelled) setSession(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    let cancelled = false;
    getFinishedGames({ sessionId: id })
      .then((data) => {
        if (!cancelled) setFinishedGames(data);
      })
      .catch(() => {
        // Finished-games list is must not block the page.
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  // Adding games is owner-only server-side and SessionResponse carries no owner
  // field. getSessions() is owner-scoped, so checking that list
  useEffect(() => {
    let cancelled = false;
    getSessions()
      .then((data) => {
        if (!cancelled) {
          setIsOwner(data.some((s) => String(s.id) === String(id)));
        }
      })
      .catch((err) =>
        // A 404 means the user owns no sessions at all
        setIsOwner(err.status !== 404)
      );
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (error) {
    if (error.status === 404) {
      return (
        <div className="page session-detail-page">
          <p className="error-message">Session not found</p>
          {BackToSessions}
        </div>
      );
    }
    return (
      <div className="page session-detail-page">
        <p className="error-message">
          Failed to load session: {error.message}
        </p>
        {BackToSessions}
      </div>
    );
  }

  if (session === null) {
    return (
      <div className="page session-detail-page">
        <p className="state-message">Loading session...</p>
      </div>
    );
  }

  const gamemodeCounts = session.gametypes.reduce((counts, gametype) => {
    counts[gametype.gamemode] = (counts[gametype.gamemode] || 0) + 1;
    return counts;
  }, {});

  const handleGamesAdded = (updated) => {
    setSession(updated);
    // Adding games can un-exhaust a gametype, so the stale "all finished"
    // is removed
    setFinished({});
  };

  // The PUT returns no body, so the new isActive value has to be refetched.
  const handleFinished = async () => {
    const fresh = await getSessionById(id);
    setSession(fresh);
  };

  const handleStart = async (gamemode) => {
    try {
      const res = await startGame({ sessionId: id, gameType: gamemode });
      navigate(`/session/${id}/game/${res.gameId}`);
    } catch (err) {
      if (err.status === 404) {
        setFinished((f) => ({
          ...f,
          [gamemode]: 'All games of this type are finished',
        }));
      } else {
        setActionError(`Failed to start game: ${err.message}`);
      }
    }
  };

  return (
    <div className="page session-detail-page">
      <div className="session-detail-header">
        {BackToSessions}
        <h1>Session #{id}</h1>
      </div>
      <div className="session-detail-meta">
        <span className="session-card-date">
          {new Date(session.date).toLocaleDateString()}
        </span>
        {session.isActive && (
          <span className="session-card-badge">Active</span>
        )}
      </div>
      {actionError && <p className="error-message">{actionError}</p>}
      <div className="session-detail-actions">
        {isOwner && session.isActive ? (
          <button
            className="btn btn-danger"
            type="button"
            onClick={() => setFinishingSession(true)}
          >
            Finish session
          </button>
        ) : (
          !session.isActive && (
            <span className="session-finished-badge">Finished</span>
          )
        )}
      </div>
      <div className="session-detail-games-header">
        <h2>Games in this session</h2>
        {isOwner && session.isActive && (
          <button
            className="btn btn-primary"
            type="button"
            onClick={() => setAddingGames(true)}
          >
            Add game
          </button>
        )}
      </div>
      {Object.keys(gamemodeCounts).length === 0 ? (
        <p className="state-message session-detail-empty">
          No games in this session
        </p>
      ) : (
        <div className="session-detail-games">
          {Object.entries(gamemodeCounts).map(([gamemode, count]) => (
            <GameTypeCard
              key={gamemode}
              gamemode={gamemode}
              count={count}
              onStart={() => handleStart(gamemode)}
              finishedMessage={finished[gamemode]}
              playable={session.isActive}
            />
          ))}
        </div>
      )}
      <h2>Finished games</h2>
      <FinishedGamesList games={finishedGames} sessionId={id} />
      {addingGames && (
        <AddGamesModal
          sessionId={id}
          onClose={() => setAddingGames(false)}
          onAdded={handleGamesAdded}
        />
      )}
      {finishingSession && (
        <ConfirmFinishModal
          sessionId={id}
          onClose={() => setFinishingSession(false)}
          onFinished={handleFinished}
        />
      )}
    </div>
  );
}