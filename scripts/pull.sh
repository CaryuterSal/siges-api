#!/bin/bash
set -e

echo "Jalando variables desde SSM..."

get_param() {
  aws ssm get-parameter \
    --name "/siges/$1" \
    --with-decryption \
    --region us-east-1 \
    --query "Parameter.Value" \
    --output text
}

cat > /home/ubuntu/siges/.env <<EOF
SPRING_DATASOURCE_USERNAME=$(get_param SPRING_DATASOURCE_USERNAME)
SPRING_DATASOURCE_PASSWORD=$(get_param SPRING_DATASOURCE_PASSWORD)
SPRING_DATASOURCE_URL=$(get_param SPRING_DATASOURCE_URL)
SPRING_DATA_REDIS_URL=$(get_param SPRING_DATA_REDIS_URL)
DOCKER_HUB_USERNAME=$(get_param docker/username)
SECURITY_JWT_SECRET=$(get_param SECURITY_JWT_SECRET)
SPRING_MAIL_USERNAME=$(get_param SPRING_MAIL_USERNAME)
SPRING_MAIL_PASSWORD=$(get_param SPRING_MAIL_PASSWORD)
EOF

echo "Haciendo docker login..."
DOCKER_TOKEN=$(get_param docker/token)
DOCKER_USER=$(get_param docker/username)
echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USER" --password-stdin

echo "Jalando imagen..."
cd /home/ubuntu/siges
docker compose -f compose.yaml -f compose.prod.yaml pull