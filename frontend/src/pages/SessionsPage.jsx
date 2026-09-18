import { useState } from 'react';
import SessionsList from '../components/SessionsList';
import CreateSession from '../components/CreateSession';

export default function SessionsPage() {
  const [showCreate, setShowCreate] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

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
      <SessionsList key={refreshKey} />
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
