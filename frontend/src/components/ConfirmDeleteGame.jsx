import { useState } from 'react';
import { deleteGame } from '../api';
import ConfirmDialog from './ConfirmDialog';

export default function ConfirmDeleteGame({
  sessionId,
  gamemode,
  count,
  onClose,
  onDeleted,
}) {
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);

  const extra =
    count > 1 ? ` ${count} games of this type are in this session.` : '';
  const body = `Removes the next unstarted ${gamemode} game. Games already in progress are kept.${extra}`;

  async function handleDelete() {
    setDeleting(true);
    setError('');
    try {
      await deleteGame({ sessionId, gamemode });
      onDeleted();
      onClose();
    } catch (err) {
      setError(
        err.status === 403
          ? 'Only the session owner can remove games from this session'
          : err.status === 404
            ? `No unstarted ${gamemode} game left to remove`
            : err.message
      );
      setDeleting(false);
    }
  }

  return (
    <ConfirmDialog
      title="Remove this game?"
      body={body}
      confirmLabel="Remove game"
      busyLabel="Removing…"
      error={error}
      busy={deleting}
      onConfirm={handleDelete}
      onCancel={onClose}
    />
  );
}
