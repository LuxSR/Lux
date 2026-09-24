export default function GameStatsTable({ players, winner }) {
  return (
    <table className="game-stats">
      <thead>
        <tr>
          <th>Player</th>
          <th className="game-stats-num">Points</th>
          <th className="game-stats-num">Turns</th>
          <th className="game-stats-num">Highest</th>
          <th className="game-stats-num">180s</th>
          <th className="game-stats-num">Bullseyes</th>
        </tr>
      </thead>
      <tbody>
        {players.map((player) => (
          <tr
            key={player.username}
            className={
              player.username === winner ? 'game-stats-winner' : undefined
            }
          >
            <td>{player.username}</td>
            <td className="game-stats-num">{player.points}</td>
            <td className="game-stats-num">{player.turns}</td>
            <td className="game-stats-num">{player.highestScore}</td>
            <td className="game-stats-num">{player.triple20s}</td>
            <td className="game-stats-num">{player.bullseyes}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}