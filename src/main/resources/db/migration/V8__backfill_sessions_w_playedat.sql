UPDATE sessions
SET played_at = NOW()
WHERE played_at IS NULL;