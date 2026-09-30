import { useEffect, useState } from 'react';
import { getAllGamemodes } from '../api';
import SessionsList from '../components/SessionsList';
import CreateSession from '../components/CreateSession';

export default function SessionsPage() {
  const [showCreate, setShowCreate] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [availableGamemodes, setAvailableGamemodes] = useState(null);
  const [gamemodeError, setGamemodeError] = useState('');
  const [gamemode, setGamemode] = useState('');

  useEffect(() => {
    getAllGamemodes()
      .then((data) => setAvailableGamemodes(data))
      .catch((err) => setGamemodeError(err.message));
  }, []);

  return (
    <div className="page sessions-page">
      <div className="sessions-header">
        <h1>Sessions</h1>
        <button
          className="btn btn-primary"
          type="button"
          onClick={() => setShowCreate(true)}
        >
          Create Session
        </button>
      </div>
      <div className="sessions-filter" aria-label="Show all">
        {availableGamemodes === null && !gamemodeError && (
          <p className="state-message">Loading gamemodes...</p>
        )}
        {gamemodeError && <p className="error-message">Failed to load gamemodes: {gamemodeError}</p>}
        {availableGamemodes && (
          <div className="gamemode-picker-row">
            <select
              value={gamemode}
              onChange={(e) => setGamemode(e.target.value)}
              aria-label="Show all"
            >
              <option value="">Show all</option>
              {availableGamemodes.map((gametype) => (
                <option key={gametype.gamemode} value={gametype.gamemode}>
                  {gametype.gamemode}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>
      <SessionsList key={refreshKey} gamemode={gamemode} />
      {showCreate && (
        <CreateSession
          onClose={() => setShowCreate(false)}
          onCreated={() => {
            setRefreshKey((k) => k + 1);
            setShowCreate(false);
          }}
        />
      )}
    </div>
  );
}
