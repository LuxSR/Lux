import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="page not-found-page">
      <h1>404</h1>
      <p>Page not found</p>
      <Link className="btn btn-primary" to="/">
        Back to Home
      </Link>
    </div>
  );
}