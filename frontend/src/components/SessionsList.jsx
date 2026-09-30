import { useEffect, useState } from 'react';
import { getSessions } from '../api';
import SessionCard from './SessionCard';

export default function SessionsList({ gamemode }) {
  const [sessions, setSessions] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setSessions(null);
    setError('');
    getSessions({ gamemode })
      .then((data) => {
        if (!cancelled) setSessions(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err);
      });
    return () => {
      cancelled = true;
    };
  }, [gamemode]);

  if (error) {
    if (error.status === 404) {
      return (
        <p className="state-message">
          {gamemode ? `No sessions with ${gamemode} games` : 'No sessions yet'}
        </p>
      );
    }
    return (
      <p className="error-message">Failed to load sessions: {error.message}</p>
    );
  }

  if (sessions === null) {
    return <p className="state-message">Loading sessions...</p>;
  }

  if (sessions.length === 0) {
    return (
      <p className="state-message">
        {gamemode ? `No sessions with ${gamemode} games` : 'No sessions yet'}
      </p>
    );
  }

  return (
    <div className="sessions-list">
      {sessions.map((session) => (
        <SessionCard key={session.id} session={session} />
      ))}
    </div>
  );
}
