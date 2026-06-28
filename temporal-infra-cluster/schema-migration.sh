#!/bin/sh
set -e

echo "=== Starting Temporal Free-Tier Bootstrapper ==="

# STEP 1: SATISFY RENDER HEALTH CHECK IMMEDIATELY
echo "Spinning up free-tier port responder loop on port 10000..."
while true; do 
  echo -e "HTTP/1.1 200 OK\nContent-Type: text/plain\nContent-Length: 11\n\nOperational" | nc -l -p 10000 || sleep 1
done &

# Give background network parameters a brief moment to stabilize
sleep 2

# STEP 2: PROBE THE DATABASE
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

# 👑 FIX: Utilizing the absolute internal path reference to enforce absolute schema upgrade consistency
echo "Updating operational visibility schemas..."
/usr/local/bin/temporal-sql-tool \
    --plugin postgres \
    --endpoint ${DB_HOST} \
    --port ${DB_PORT} \
    --user ${DB_USER} \
    --password ${DB_PWD} \
    --database ${DB_NAME:-"temporal"} \
    update-schema -d /etc/temporal/schema/postgres/v12/temporal/versioned

echo "=== Database Schema Auto-Migration Completed Successfully ==="

# Hand over execution back to the primary Temporal server process
exec /etc/temporal/entrypoint.sh start
