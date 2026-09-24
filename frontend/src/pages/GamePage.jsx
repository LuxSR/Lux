import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getGameState, getUsername, playRound } from '../api';

// Array of all numbers from 1-20+25
const DART_VALUES = Array.from({ length: 20 }, (_, i) => i + 1).concat(25);

function emptySlots() {
  return [
    { value: '', multiplier: '' },
    { value: '', multiplier: '' },
    { value: '', multiplier: '' },
  ];
}

export default function GamePage() {
  const { id, gameId } = useParams();
  const [game, setGame] = useState(null);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [slots, setSlots] = useState(emptySlots);

  useEffect(() => {
    let cancelled = false;
    getGameState({ sessionId: id, gameId })
      .then((data) => {
        if (!cancelled) setGame(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      });
    return () => {
      cancelled = true;
    };
  }, [id, gameId]);

  if (error) {
    return (
      <div className="page game-page">
        <p className="error-message">Failed to load game: {error}</p>
        <Link className="btn session-detail-back" to={`/session/${id}`}>
          &larr; Back to Session
        </Link>
      </div>
    );
  }

  if (game === null) {
    return (
      <div className="page game-page">
        <p className="state-message">Loading game...</p>
      </div>
    );
  }

  const username = getUsername(localStorage.getItem('token'));
  const finished = game.isFinished;
  const roundTotal = slots.reduce((sum, slot) => {
    const value = Number(slot.value);
    const multiplier = Number(slot.multiplier);
    return sum + (value > 0 && multiplier > 0 ? value * multiplier : 0);
  }, 0);
  const allFilled = slots.every(
    (slot) => slot.value !== '' && slot.multiplier !== ''
  );

  function updateSlot(index, field, value) {
    setSlots((prev) => {
      const next = prev.map((slot, i) =>
        i === index ? { ...slot, [field]: value } : slot
      );
      if (
        field === 'value' &&
        (Number(value) === 0 || Number(value) === 25)
      ) {
        next[index].multiplier = 1;
      }
      return next;
    });
  }

  async function handleSubmit() {
    setSubmitting(true);
    setActionError('');
    try {
      const score = slots
        .filter((slot) => Number(slot.value) > 0)
        .map((slot) => `${slot.value} ${slot.multiplier}`)
        .join(' ');
      await playRound({ sessionId: id, gameId, username, score });
      const fresh = await getGameState({ sessionId: id, gameId });
      setGame(fresh);
      setSlots(emptySlots());
    } catch (err) {
      setActionError(`Failed to register round: ${err.message}`);
    } finally {
      setSubmitting(false);
    }
  }

  const multipliersFor = (slot) =>
    Number(slot.value) === 0
      ? [1]
      : Number(slot.value) === 25
        ? [1, 2]
        : [1, 2, 3];

  return (
    <div className="page game-page">
      <div className="game-page-header">
        <Link className="btn session-detail-back" to={`/session/${id}`}>
          &larr; Back to Session
        </Link>
        <h1>Game #{gameId}</h1>
      </div>

      <h2>Scoreboard</h2>
      <div className="game-scoreboard">
        {game.players.map((player) => (
          <div
            key={player.username}
            className={`game-scoreboard-row${
              player.username === game.turn ? ' game-scoreboard-turn' : ''
            }`}
          >
            <span>{player.username}</span>
            <span>{player.points}</span>
          </div>
        ))}
      </div>

      {finished ? (
        <div className="game-winner">
          <h2>Winner: {game.winner || game.turn}</h2>
          <p>Game finished</p>
        </div>
      ) : (
        <div className="game-round-panel">
          <h2>Your round — {username}</h2>
          <div className="game-dart-slots">
            {slots.map((slot, index) => (
              <div key={index} className="game-dart-slot">
                <label>
                  <span>D{index + 1} number</span>
                  <select
                    value={slot.value}
                    onChange={(e) =>
                      updateSlot(index, 'value', e.target.value)
                    }
                    disabled={submitting}
                  >
                    <option value="">—</option>
                    <option value="0">Miss (0)</option>
                    {DART_VALUES.map((value) => (
                      <option key={value} value={value}>
                        {value}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  <span>Multiplier</span>
                  <select
                    value={slot.multiplier}
                    onChange={(e) =>
                      updateSlot(index, 'multiplier', e.target.value)
                    }
                    disabled={submitting || Number(slot.value) === 0}
                  >
                    <option value="">—</option>
                    {multipliersFor(slot).map((mult) => (
                      <option key={mult} value={mult}>
                        &times;{mult}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            ))}
          </div>
          <div className="game-round-total">Round total: {roundTotal}</div>
          {actionError && <p className="error-message">{actionError}</p>}
          <button
            type="button"
            className="btn game-register"
            onClick={handleSubmit}
            disabled={!allFilled || submitting}
          >
            {submitting ? 'Registering...' : 'Register Round'}
          </button>
        </div>
      )}
    </div>
  );
}