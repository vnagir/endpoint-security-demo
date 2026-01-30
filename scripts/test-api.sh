#!/bin/bash
# Test API endpoints. Run after analytics-service is up and some data is ingested.
BASE="${BASE_URL:-http://localhost:8080}"

echo "=== Health ==="
curl -s "$BASE/actuator/health" | head -5

echo ""
echo "=== Unique endpoint IDs (analyst token) ==="
curl -s -H "Authorization: Bearer analyst-token-67890" "$BASE/api/v1/endpoints"

echo ""
echo "=== Summary (analyst token) - replace ENDPOINT_ID with a real UUID from your data ==="
curl -s -H "Authorization: Bearer analyst-token-67890" \
  "$BASE/api/v1/summary/550e8400-e29b-41d4-a716-446655440000"

echo ""
echo "=== Alerts (admin token) ==="
curl -s -H "Authorization: Bearer admin-token-12345" \
  "$BASE/api/v1/alerts?min_score=1"

echo ""
echo "=== Alerts forbidden for analyst (expect 403) ==="
curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer analyst-token-67890" \
  "$BASE/api/v1/alerts"
