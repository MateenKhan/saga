#!/bin/sh

# Exit immediately if any step fails
set -e

echo "=== Starting Temporal Database Schema Auto-Migration ==="

# Wait for PostgreSQL to become responsive using Temporal's native migration engine
echo "Probing PostgreSQL database at ${DB_HOST:-"localhost"}:${DB_PORT:-5432}..."
until /usr/local/bin/temporal-sql-tool \
    --plugin postgres \
    --endpoint ${DB_HOST:-"localhost"} \
    --port ${DB_PORT:-5432} \
    --user ${DB_USER} \
    --password ${DB_PWD} \
    --database "postgres" \
    list-databases >/dev/null 2>&1; do
  echo "PostgreSQL port is not responding yet - sleeping for 3 seconds..."
  sleep 3
done
echo "PostgreSQL cluster connection successfully verified."

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
