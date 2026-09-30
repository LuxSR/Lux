import { useState } from 'react';
import { deleteSession } from '../api';
import ConfirmDialog from './ConfirmDialog';

export default function ConfirmDeleteSession({
  sessionId,
  onClose,
  onDeleted,
}) {
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);

  async function handleDelete() {
    setDeleting(true);
    setError('');
    try {
      await deleteSession({ sessionId });
      onDeleted();
      onClose();
    } catch (err) {
      setError(
        err.status === 403
          ? 'Only the session owner can delete this session'
          : err.status === 404
            ? 'Session not found'
            : err.message
      );
      setDeleting(false);
    }
  }

  return (
    <ConfirmDialog
      title="Delete this session?"
      body="This permanently removes the session, its games and all recorded stats. It cannot be undone."
      confirmLabel="Delete session"
      busyLabel="Deleting…"
      error={error}
      busy={deleting}
      onConfirm={handleDelete}
      onCancel={onClose}
    />
  );
}
