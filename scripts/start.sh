#!/bin/bash
set -e
cd /home/ubuntu/siges
docker compose -f compose.yaml -f compose.prod.yaml up -d
docker image prune -f