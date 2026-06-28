# Use the official production-ready Temporal Server image as the base
FROM temporalio/server:1.24.2

USER root

# 👑 FIX: Utilizing Alpine's native package manager to securely inject netcat
RUN apk update && apk add --no-cache netcat-openbsd

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
