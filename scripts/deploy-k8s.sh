#!/bin/sh
set -eu
: "${MYSQL_PASSWORD:?Supply MYSQL_PASSWORD}"
: "${MYSQL_ROOT_PASSWORD:?Supply MYSQL_ROOT_PASSWORD}"
image=${1:-events-logistics:local}
kubectl --context kind-events-devops apply -f k8s/namespace.yaml
set +x
kubectl --context kind-events-devops -n events create secret generic events-db \
  --from-literal=password="$MYSQL_PASSWORD" --from-literal=root-password="$MYSQL_ROOT_PASSWORD" \
  --dry-run=client -o yaml | kubectl --context kind-events-devops apply -f -
kubectl --context kind-events-devops apply -k k8s
kubectl --context kind-events-devops -n events rollout status statefulset/mysql --timeout=300s
kubectl --context kind-events-devops -n events set image deployment/events-app "app=$image"
if [ "$image" = events-logistics:local ]; then
  kubectl --context kind-events-devops -n events rollout restart deployment/events-app
fi
kubectl --context kind-events-devops -n events rollout status deployment/events-app --timeout=300s
