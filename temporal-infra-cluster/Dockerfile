# Use the official production-ready Temporal Server image as the base
FROM temporalio/server:1.24.2

USER root

# Install standard netcat for handling Render free-tier port scans
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Copy the migration bootstrapper script into the container workspace
COPY schema-migration.sh /usr/local/bin/schema-migration.sh

# Grant full executable access permissions to the entry script
RUN chmod +x /usr/local/bin/schema-migration.sh

# Change back to the standard safe user profile
USER temporal

# Expose both the custom fake Render port and Temporal's actual gRPC engine port
EXPOSE 10000
EXPOSE 7233

# Direct the container entrypoint execution loop to our migration script
ENTRYPOINT ["/usr/local/bin/schema-migration.sh"]
