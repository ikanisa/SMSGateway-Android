#!/bin/bash
# Apply migration using Supabase REST API with service role key
# Alternative: Use Dashboard SQL Editor (recommended)

PROJECT_URL="https://wadhydemushqqtcrrlwm.supabase.co"
SERVICE_ROLE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhZGh5ZGVtdXNocXF0Y3JybHdtIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NTc0MTU1NCwiZXhwIjoyMDgxMzE3NTU0fQ.mQg8USbqggCTUinPPhsvdqFl1j8baX71ulUvVdGYL7s"
MIGRATION_FILE="supabase/migrations/20250129_create_transactions_table.sql"

echo "Attempting to apply migration via Supabase REST API..."
echo "Note: This requires the SQL to be executed via a function or Dashboard"
echo ""
echo "RECOMMENDED: Apply migration via Dashboard SQL Editor:"
echo "1. Go to: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/sql/new"
echo "2. Copy contents of: $MIGRATION_FILE"
echo "3. Paste and Run"
echo ""
echo "Or use psql if you have database password:"
echo "psql 'postgresql://postgres:[PASSWORD]@db.wadhydemushqqtcrrlwm.supabase.co:5432/postgres' < $MIGRATION_FILE"
