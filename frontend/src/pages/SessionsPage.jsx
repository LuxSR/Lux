import { useState } from 'react';

export default function SessionsPage() {
  const [, setShowCreate] = useState(false);
  // TODO: do something with the showCreate state

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
      <div className="sessions-list">{/* TODO: add SessionsList */}</div>
    </div>
  );
}
