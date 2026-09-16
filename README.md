# Lux

## Contributions
The code follows the standard Java coding conventions and is formatted using Sun Code Conventions. 
Branches follow the GitFlow model, and are named according to the issue they are addressing with type prefix, e.g. feat/issue-1234.
Pull requests are made against dev branch, dev will be merged into main.

The project has the structure:

    ├── src/
    │   └── main/
    │       └── java
    │            └── lux/
    │                └── dartgame/
    │                    └── controller/
    │                    └── model/
    │                    └── repository/
    │                    └── service/
    └── resources/
        └── db/
            └── migration/

## Database

The project uses PostgreSQL with Flyway for schema management and seeding.

### Setup

1. Create a PostgreSQL database named `dartgame`
2. Update connection details in `src/main/resources/application.properties` if needed

### Seeding

Seed data runs automatically on startup via Flyway migrations:

- `V1__init_schema.sql` - Creates all tables
- `V2__seed_data.sql` - Seeds initial data

The seed includes:
- 3 users (Alice van Buren, Bob de Groot, Luke Littler)
- 5 gametypes (501, 301, Cricket, Killer, Around the Clock)
- Sessions, games, and statistics with realistic dart data

Re-running is safe - all inserts use `ON CONFLICT DO NOTHING`.

<!-- ============================================================
REVIEW: repo-level notes (instructor review pass, 2026-09-16)

Inline comments are marked REVIEW(azure) / REVIEW(sec) / REVIEW(api) /
REVIEW(bug) / REVIEW(noob) / REVIEW(config) / REVIEW(efficiency) /
REVIEW(good) in the source. This branch exists to be read as a diff.
Do not merge it: read it, act on what you agree with, then delete it.

The findings below have no single line to attach to.

1. THE BIG ONE IS IN SessionController. GET /session takes ?username= from the
   client and returns that user's sessions without checking who is asking, and
   the Authorization header it accepts is never read. Any logged-in user can
   read anyone else's data. Every other finding in this repo is smaller than
   that one. The fix is the same everywhere: the caller's identity comes from
   the token via @AuthenticationPrincipal, never from a parameter.

2. NO FRONTEND IN THIS REPO. There is a feat/setup-frontend branch, but dev is
   backend only, and the capstone is fullstack.

3. NO DOCKERFILE AND NO COMPOSE FILE, and the datasource URL points at
   localhost. Those two together mean the app cannot currently run anywhere
   except a developer laptop with a local PostgreSQL. This is the largest
   deployment gap even though the configuration hygiene elsewhere is good.

4. NO CORS CONFIGURATION anywhere.

5. TEST COVERAGE IS TWO FILES (SessionControllerTest, SessionServiceTest) and
   jacoco is already wired up in the pom, so look at the report and aim the next
   tests at the delete rules in SessionService, which are the part most likely
   to be wrong.

6. THE README IS THE BEST OF THE FOUR on database setup. What it is missing is
   the environment contract: JWT_SECRET (and its expected format, base64) and
   POSTGRES_PWD are required or the app will not start, and that is not written
   down anywhere.
============================================================ -->
