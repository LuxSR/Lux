import { useState } from 'react';
import { finishSession } from '../api';
import ConfirmDialog from './ConfirmDialog';

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
    <ConfirmDialog
      title="Finish this session?"
      body="This cannot be undone, and no further games can be started in this session."
      confirmLabel="Finish session"
      busyLabel="Finishing…"
      error={error}
      busy={finishing}
      onConfirm={handleFinish}
      onCancel={onClose}
    />
  );
}
