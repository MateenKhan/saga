#!/bin/sh

# Exit immediately if any step fails
set -e

echo "=== Starting Temporal Database Schema Auto-Migration ==="

# Wait for PostgreSQL to become responsive within the private network
echo "Waiting for PostgreSQL database to be reachable..."
until nc -z -v -w30 ${DB_HOST:-"localhost"} ${DB_PORT:-5432}; do
  echo "PostgreSQL is unavailable - sleeping for 2 seconds..."
  sleep 2
done
echo "PostgreSQL connection successfully established."

# Setup internal connection strings out of the primary SEEDS variable
export CASSANDRA_SEEDS=${POSTGRES_SEEDS}

# Execute Temporal structural schema migrations for Postgres
echo "Initializing structural database schemas..."
/usr/local/bin/temporal-sql-tool \
    --plugin postgres \
    --endpoint ${DB_HOST} \
    --port ${DB_PORT} \
    --user ${DB_USER} \
    --password ${DB_PWD} \
    --database ${DB_NAME:-"temporal"} \
    setup-schema

echo "Updating operational visibility schemas..."
/usr/local/bin/temporal-sql-tool \
    --plugin postgres \
    --endpoint ${DB_HOST} \
    --port ${DB_PORT} \
    --user ${DB_USER} \
    --password ${DB_PWD} \
    --database ${DB_NAME:-"temporal"} \
    update-schema -d ./schema/postgres/v12/temporal/versioned

echo "=== Database Schema Auto-Migration Completed Successfully ==="

# Hand over execution back to the primary Temporal server process
exec /etc/temporal/entrypoint.sh start
