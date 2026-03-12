#!/bin/bash
sleep 20

RETRIES=5
for i in $(seq 1 $RETRIES); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    https://siges.lat/api/actuator/health)
  if [ "$STATUS" = "200" ]; then
    echo "Health check passed"
    exit 0
  fi
  echo "Intento $i fallido (status $STATUS), esperando..."
  sleep 10
done

echo "Health check failed, CodeDeploy hará rollback"
exit 1