# Commandes vérifiées

## Maven et base locale
```powershell
. ./scripts/Use-ProjectJava.ps1
mvn -version
mvn clean
mvn compile
mvn test
mvn package
mvn install
mvn verify
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Initialize-LocalDatabase.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Start-LocalApplication.ps1
```
Toutes les phases Maven ont réussi, sans masquer des tests. 20 tests au verify/install initial ; 38 tests au dernier verify et dans Jenkins #4.

## Docker
```powershell
docker compose -f docker-compose.devops.yml --profile app up -d --build app
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-TacheApi.ps1
```
API/MySQL réussis ; app/mysql Compose ensuite arrêtés pour libérer la RAM.

## Kubernetes
```powershell
.local/tools/kind.exe create cluster --name events-tache --config k8s/kind.yaml --image kindest/node:v1.32.2 --kubeconfig .local/kubeconfig --wait 180s
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Deploy-LocalKubernetes.ps1
kubectl --kubeconfig .local/kubeconfig --context kind-events-tache -n events-tache get pods,services,pvc
kubectl --kubeconfig .local/kubeconfig --context kind-events-tache -n events-tache port-forward --address 127.0.0.1 service/events 18089:8089
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-TacheApi.ps1 -BaseUrl http://localhost:18089/events
```
Garder le port-forward actif dans un autre terminal. Le script crée un participant de
preuve qu'il conserve sans rôle. Les archives explicites de plateforme corrigent
l'import containerd multi-plateforme ; Docker Desktop API 1.48 les prend en charge.

## Terraform (apply déjà approuvé et exécuté)
```powershell
.local/tools/terraform.exe '-chdir=terraform' fmt
.local/tools/terraform.exe '-chdir=terraform' init -input=false
.local/tools/terraform.exe '-chdir=terraform' validate
.local/tools/terraform.exe '-chdir=terraform' plan '-out=monitoring.tfplan'
.local/tools/terraform.exe '-chdir=terraform' apply monitoring.tfplan
.local/tools/terraform.exe '-chdir=terraform' plan -detailed-exitcode
```
Résultat : six créations puis zéro différence. Toute modification suivante exige
un nouveau plan et un accord avant apply. State et plans ignorés par Git.

## Preuves et PowerPoint
```powershell
kubectl --kubeconfig .local/kubeconfig --context kind-events-tache -n events-monitoring port-forward --address 127.0.0.1 service/prometheus 19090:9090
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Capture-BaselineEvidence.ps1
python scripts/Capture-ServiceDashboards.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Build-BaselinePresentation.ps1
```
Utilise les outils déjà installés : Windows Forms, Edge/Playwright, PowerPoint.
Ne pas afficher .env, docker compose config ou les Secrets dans les preuves.
La variante `docker compose ... config --quiet` vérifie la syntaxe sans exposer les valeurs.

## Intervention de compactage (préparée, non exécutée)
L'agent n'a pas de droits administrateur. Après sauvegarde des travaux WSL, ouvrir
PowerShell en tant qu'administrateur et lancer depuis la racine du projet :
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Compact-DockerDisk.ps1 -AllowWslShutdown
```
Ce script arrête Docker et toutes les WSL ; ne pas l'exécuter pendant un travail WSL.
Il ne formate pas le disque, ne supprime pas de volume et ne change pas sa capacité maximale.
Après relance de Docker :
```powershell
docker start events-tache-control-plane
docker compose -f docker-compose.devops.yml --profile nexus up -d nexus
kubectl --kubeconfig .local/kubeconfig --context kind-events-tache -n events-tache get pods
```
Relancer les deux port-forwards documentés, puis reprendre Jenkins/Sonar.

## Livraison complète locale et publication publique
python scripts/Run-LocalPipeline.py --full
python scripts/Run-LocalPipeline.py --full --publish
python scripts/Verify-SonarAnalysis.py --build 4
python scripts/Verify-LocalDelivery.py --build 4

Le numéro 4 désigne le dernier build de livraison entièrement vérifié au moment
de cette commande ; employer le dernier build réussi après la nouvelle livraison.
La publication publique est autorisée uniquement vers ayyoub1/events-tache.

## Grafana et captures
python scripts/Initialize-LocalGrafana.py
powershell -File scripts/Start-LocalForwards.ps1 -Restart -IncludeGrafana
python scripts/Verify-Grafana.py
python scripts/Capture-Grafana.py
python scripts/Capture-CiDashboards.py --build 4
python scripts/Capture-ServiceDashboards.py
powershell -File scripts/Capture-BaselineEvidence.ps1
powershell -File scripts/Build-BaselinePresentation.ps1
python scripts/Validate-Presentation.py
