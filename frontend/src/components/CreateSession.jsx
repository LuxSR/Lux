import { useEffect, useState } from 'react';
import { createSession, getAllGamemodes, getUsername, MAX_TOTAL_PLAYERS } from '../api';

const MAX_ADDED_PLAYERS = MAX_TOTAL_PLAYERS - 1;

export default function CreateSession({ onClose, onCreated }) {
  const [available, setAvailable] = useState(null);
  const [selected, setSelected] = useState([]);
  const [pick, setPick] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const [players, setPlayers] = useState([]);
  const [playerInput, setPlayerInput] = useState('');
  const [playerError, setPlayerError] = useState('');

  const owner = getUsername(localStorage.getItem('token'));

  useEffect(() => {
    getAllGamemodes()
      .then((data) => setAvailable(data))
      .catch((err) => setError(err.message));
  }, []);

  function addGamemode() {
    if (pick) {
      setSelected([...selected, pick]);
      setPick('');
    }
  }

  function removeGamemode(index) {
    setSelected(selected.filter((_, i) => i !== index));
  }

  function addPlayer() {
    // TODO: consider trimming surrounding whitespace before exact-match (backend does not trim)
    const username = playerInput;
    if (!username.trim()) {
      setPlayerError('Enter a username');
      return;
    }
    if (username === owner) {
      setPlayerError('That is you — you are already in the session');
      return;
    }
    if (players.includes(username)) {
      setPlayerError('That username is already added');
      return;
    }
    if (players.length >= MAX_ADDED_PLAYERS) {
      setPlayerError(`Max ${MAX_TOTAL_PLAYERS} players in total (including you)`);
      return;
    }
    setPlayers([...players, username]);
    setPlayerInput('');
    setPlayerError('');
  }

  function removePlayer(index) {
    setPlayers(players.filter((_, i) => i !== index));
    setPlayerError('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await createSession({ gamemodes: selected, players });
      onCreated();
    } catch (err) {
      setError(
        err.status === 404
          ? 'One or more usernames not found — check the spelling'
          : err.message
      );
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        onClick={(e) => e.stopPropagation()}
      >
        <h2>Create Session</h2>
        <form className="form" onSubmit={handleSubmit}>
          {available === null && !error && (
            <p className="state-message">Loading gamemodes...</p>
          )}
          {error && <p className="error-message">{error}</p>}
          {available && (
            <div className="gamemode-picker">
              <div className="gamemode-picker-row">
                <select value={pick} onChange={(e) => setPick(e.target.value)}>
                  <option value="">Select gamemode...</option>
                  {available.map((gametype) => (
                    <option key={gametype.gamemode} value={gametype.gamemode}>
                      {gametype.gamemode}
                    </option>
                  ))}
                </select>
                <button className="btn" type="button" onClick={addGamemode}>
                  Add
                </button>
              </div>
              {selected.length > 0 && (
                <div className="gamemode-pills">
                  {selected.map((gamemode, index) => (
                    <span key={index} className="gamemode-pill">
                      {gamemode}
                      <button
                        type="button"
                        onClick={() => removeGamemode(index)}
                        aria-label={`Remove ${gamemode}`}
                      >
                        &times;
                      </button>
                    </span>
                  ))}
                </div>
              )}
            </div>
          )}
          <div className="player-picker">
            <div className="player-picker-header">
              <h3>Players</h3>
              <span className="player-counter">
                {players.length + 1} / {MAX_TOTAL_PLAYERS}
              </span>
            </div>
            <div className="player-chips">
              <span className="player-chip player-chip-owner">{owner} (you)</span>
              {players.map((username, index) => (
                <span key={index} className="player-chip">
                  {username}
                  <button
                    type="button"
                    onClick={() => removePlayer(index)}
                    aria-label={`Remove ${username}`}
                  >
                    &times;
                  </button>
                </span>
              ))}
            </div>
            <div className="player-picker-row">
              <input
                className="player-input"
                type="text"
                value={playerInput}
                onChange={(e) => {
                  setPlayerInput(e.target.value);
                  setPlayerError('');
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    addPlayer();
                  }
                }}
                placeholder="Opponent username"
              />
              <button className="btn" type="button" onClick={addPlayer}>
                Add
              </button>
            </div>
            {playerError && <p className="error-message">{playerError}</p>}
            <p className="player-hint">
              Enter usernames exactly as registered — no search yet.
            </p>
          </div>
          <div className="modal-actions">
            <button className="btn" type="button" onClick={onClose}>
              Cancel
            </button>
            <button
              className="btn btn-primary"
              type="submit"
              disabled={selected.length === 0 || submitting}
            >
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}