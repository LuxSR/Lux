import { useEffect, useState } from 'react';
import { getSessions } from '../api';
import SessionCard from './SessionCard';

export default function SessionsList() {
  const [sessions, setSessions] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    getSessions()
      .then((data) => {
        if (!cancelled) setSessions(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) {
    if (error.status === 404) {
      return <p className="state-message">No sessions yet</p>;
    }
    return (
      <p className="error-message">Failed to load sessions: {error.message}</p>
    );
  }

  if (sessions === null) {
    return <p className="state-message">Loading sessions...</p>;
  }

  return (
    <div className="sessions-list">
      {sessions.map((session) => (
        <SessionCard key={session.id} session={session} />
      ))}
    </div>
  );
}
