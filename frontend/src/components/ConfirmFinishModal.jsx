import { useState } from 'react';
import { finishSession } from '../api';

export default function ConfirmFinishModal({ sessionId, onClose, onFinished }) {
  const [error, setError] = useState('');
  const [finishing, setFinishing] = useState(false);

  async function handleFinish() {
    setFinishing(true);
    setError('');
    try {
      await finishSession({ sessionId });
      onFinished();
      onClose();
    } catch (err) {
      setError(
        err.status === 403
          ? 'Only the session owner can finish this session'
          : err.status === 404
            ? 'Session not found'
            : err.message
      );
      setFinishing(false);
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
        <h2>Finish this session?</h2>
        <p>
          This cannot be undone, and no further games can be started in this
          session.
        </p>
        {error && <p className="error-message">{error}</p>}
        <div className="modal-actions">
          <button className="btn" type="button" onClick={onClose}>
            Cancel
          </button>
          <button
            className="btn btn-danger"
            type="button"
            disabled={finishing}
            onClick={handleFinish}
          >
            {finishing ? 'Finishing…' : 'Finish session'}
          </button>
        </div>
      </div>
    </div>
  );
}
