#!/bin/bash
# Script to apply transactions table migration
# Using Supabase Management API

SUPABASE_ACCESS_TOKEN="sbp_d3eb3056d2f5f9e672187f498516d47774372ceb"
PROJECT_REF="wadhydemushqqtcrrlwm"
MIGRATION_FILE="supabase/migrations/20250129_create_transactions_table.sql"

echo "Applying migration: $MIGRATION_FILE"

# Read migration SQL
SQL_CONTENT=$(cat "$MIGRATION_FILE")

# Use Supabase Management API to execute SQL
# Note: This requires the Management API endpoint
curl -X POST "https://api.supabase.com/v1/projects/${PROJECT_REF}/database/query" \
  -H "Authorization: Bearer ${SUPABASE_ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"query\": $(echo "$SQL_CONTENT" | jq -Rs .)}"

echo ""
echo "Migration applied!"
