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
