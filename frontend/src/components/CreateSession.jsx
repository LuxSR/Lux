import { useEffect, useState } from 'react';
import { createSession, getAllGamemodes } from '../api';

export default function CreateSession({ onClose, onCreated }) {
  const [available, setAvailable] = useState(null);
  const [selected, setSelected] = useState([]);
  const [pick, setPick] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

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

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await createSession({ gamemodes: selected });
      onCreated();
    } catch (err) {
      setError(err.message);
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
