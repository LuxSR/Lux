import { useState } from 'react';

export default function GameTypeCard({
  gamemode,
  count,
  onStart,
  onDelete,
  deletable = false,
  finishedMessage,
  playable = true,
}) {
  const [loading, setLoading] = useState(false);

  const handleStart = async () => {
    setLoading(true);
    try {
      await onStart();
    } finally {
      setLoading(false);
    }
  };

  const label = `${gamemode}${count > 1 ? ` x${count}` : ''}`;

  return (
    <div className="card game-type-card">
      <span className="game-type-card-label">{label}</span>
      <div className="game-type-card-actions">
        {deletable && (
          <button type="button" className="btn btn-danger" onClick={onDelete}>
            Remove
          </button>
        )}
        {finishedMessage ? (
          <span className="game-type-card-finished">{finishedMessage}</span>
        ) : (
          playable && (
            <button
              type="button"
              className="btn btn-primary game-type-card-start"
              onClick={handleStart}
              disabled={loading}
            >
              {loading ? 'Starting…' : 'Start'}
            </button>
          )
        )}
      </div>
    </div>
  );
}