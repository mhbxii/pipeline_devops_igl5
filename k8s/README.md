# Kubernetes Manifests — Events Project

This directory contains Kubernetes manifests for deploying the **Events Project** Spring Boot application with a MySQL database.

## Namespace Structure

All resources are deployed into the `events-project` namespace to provide isolation:

| Resource | Kind | Description |
|---|---|---|
| `namespace.yaml` | Namespace | Creates the `events-project` namespace |
| `configmap.yaml` | ConfigMap | Non-sensitive application configuration (Spring profiles, server port, JPA settings) |
| `secret.yaml` | Secret | Database credentials (**template only — see below**) |
| `mysql-deployment.yaml` | Deployment + Service | MySQL 8.0 database with a ClusterIP service |
| `deployment.yaml` | Deployment | Spring Boot app (2 replicas, rolling update) |
| `service.yaml` | Service | LoadBalancer exposing the app on port 80 |

## Creating the Secret

> **⚠️ Do NOT commit real credentials to version control.**

The provided `secret.yaml` contains placeholder values. For production, create the secret manually:

```bash
kubectl create secret generic events-db-secret \
  --namespace events-project \
  --from-literal=SPRING_DATASOURCE_URL='jdbc:mysql://events-mysql:3306/eventsProject?createDatabaseIfNotExist=true&useUnicode=true&serverTimezone=UTC' \
  --from-literal=SPRING_DATASOURCE_USERNAME='root' \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<YOUR_SECURE_PASSWORD>'
```

## Deploying

Apply the manifests **in order** (namespace must exist before other resources):

```bash
# 1. Create the namespace
kubectl apply -f k8s/namespace.yaml

# 2. Create config and secrets
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml        # or create manually (see above)

# 3. Deploy MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# 4. Deploy the application
kubectl apply -f k8s/deployment.yaml

# 5. Expose the application
kubectl apply -f k8s/service.yaml
```

Or apply everything at once (kubectl handles ordering within a namespace):

```bash
kubectl apply -f k8s/
```

> **Note:** The `deployment.yaml` uses `IMAGE_PLACEHOLDER` as the container image. Jenkins replaces this with the actual image tag (`{DOCKERHUB_USERNAME}/events-project:{BUILD_NUMBER}`) during the CI/CD pipeline using `sed`.

## Verifying the Deployment

```bash
# Check all pods are running
kubectl get pods -n events-project

# Check services and their endpoints
kubectl get svc -n events-project

# Check deployment rollout status
kubectl rollout status deployment/events-project -n events-project

# View application logs
kubectl logs -l app=events-project -n events-project --tail=100
```

## Accessing the Application

Once deployed, get the external IP of the LoadBalancer service:

```bash
kubectl get svc events-project-service -n events-project
```

The `EXTERNAL-IP` column shows the address. The app is available at:

```
http://<EXTERNAL-IP>/events/
```

> **Note:** It may take a few minutes for the LoadBalancer to provision and assign an external IP.

## Cost Considerations

> **⚠️ LoadBalancer creates an AWS Elastic Load Balancer (~$0.025/hour ≈ $18/month).**

To save costs during development, switch to `NodePort`:

```yaml
# In service.yaml, change:
spec:
  type: NodePort    # instead of LoadBalancer
```

Then access the app via `http://<NODE-IP>:<NODE-PORT>/events/`.

## Cleanup

To remove **all** resources, simply delete the namespace:

```bash
kubectl delete namespace events-project
```

This removes every resource (pods, services, secrets, configmaps) within the namespace.
