# CI/CD locale : configuration et résultats vérifiés

Le build Jenkins #4 a exécuté avec succès compilation, 38 tests, JaCoCo, Sonar,
quality gate, package, Maven deploy vers Nexus, image Docker, import kind,
rollout et vérifications HTTP. Preuves : docs/evidence/08-jenkins/build-4-*.
Le build #7 a aussi réussi publication publique et déploiement depuis Docker Hub.
Le manifeste public et les trois JAR (Jenkins/Nexus/pod) sont identiques.

## Services

Docker Compose sépare les profils jenkins, sonar et nexus. Les ports sont liés à
localhost : 8080, 9000 et 8081. Les volumes persistent après redémarrage.
Jenkins est limité à 1 Gio, Sonar à 2,5 Gio, Nexus à 1,5 Gio ; surveiller la RAM et
l'espace disque avant de lancer de nouveaux services. Sonar et Nexus sont épinglés
par digest. Sonar utilise H2 pour cette démonstration personnelle.

```powershell
docker compose -f docker-compose.devops.yml --profile jenkins --profile sonar --profile nexus up -d jenkins sonar nexus
python scripts/Configure-LocalJenkins.py
python scripts/Run-LocalPipeline.py --full
```

Initialize-LocalJenkins.ps1 et Initialize-LocalSonar.py servent à l'initialisation.
Les mots de passe générés restent dans .env ignoré ; les credentials du job sont
chiffrés par Jenkins. Ne jamais versionner ce fichier ni afficher ses valeurs.
Configure-LocalJenkins.py importe le PAT Docker Hub déjà enregistré par Docker
Desktop si son utilisateur est ayyoub1 ; le token n'est pas écrit dans les preuves.

## Outils et isolation

Le contrôleur utilise Java 21, Maven utilise Java 17, et l'analyse Sonar utilise
Java 21. Maven, Docker CLI et kubectl sont copiés depuis des images officielles ;
kind est téléchargé avec checksum. Aucun daemon Docker n'est installé dans Jenkins.
L'utilisateur jenkins accède au socket Docker : ce pouvoir reste privilégié, même
sans UID root. Ce laboratoire n'exécute que des jobs de confiance.

Le SCM est un dépôt bare local, monté en lecture seule dans Jenkins. Il permet
les builds de vrais commits sans publier la branche du groupe. Pour mettre à jour :

```powershell
git --git-dir=.local/jenkins-repo fetch origin refs/heads/Tache:refs/heads/Tache
```

Le polling H/5 surveille ce miroir local. Aucun webhook GitHub public n'est démontré.
Aucun push distant ni fusion des branches des autres membres n'a été effectué.

## Gate et livrables

Sonar local-sonar utilise un token de projet et un webhook signé HMAC. Le callback
privé était refusé : sonar.validateWebhooks=false est global à cette instance
personnelle, et non une liste d'hôtes autorisés. La signature HMAC reste vérifiée.
Le build #3 bloqué a été arrêté ; #4 a reçu le vrai callback HTTP 200 et son gate OK.
Résultat #4 : 81,9 % sur le nouveau code, 49,6 % global ; zéro vulnérabilité.

Nexus utilise le compte dédié tache-publisher et ci/settings.xml lit les variables
de credentials. Coordonnées : tn.esprit:eventsProject:1.0.0-SNAPSHOT.
L'EULA Nexus a été acceptée après accord explicite ; le script ne consent pas seul.
Verify-LocalDelivery.py compare le JAR Jenkins, le téléchargement Nexus et le JAR
dans le pod, puis vérifie l'image exacte et les replicas disponibles.

## Deux chemins de livraison

Le mode --full charge l'image dans kind sans publication Docker Hub. Le mode
suivant utilise la publication publique explicitement approuvée :

```powershell
python scripts/Run-LocalPipeline.py --full --publish
```

Il active PUBLISH_IMAGE et DEPLOY_K8S, désactive DEPLOY_LOCAL_KIND et définit
DOCKER_REPOSITORY=ayyoub1/events-tache. Le tag combine commit et numéro du build.
Ce tag unique permet la traçabilité ; un registre autorise néanmoins un écrasement
ultérieur, contrairement à une référence par digest.

Le gate est obligatoire avant tout déploiement. Les deux destinations sont
mutuellement exclusives. Le push utilise ci/PushRegistry.java (JDK 17 existant) :
export OCI linux/amd64, montage des blobs Temurin publics et PATCH de 256 Kio.
Ce contournement du proxy Docker Desktop vérifie digests et manifeste distant,
refuse un tag existant différent et ne journalise aucune URL signée.
Spécification : https://distribution.github.io/distribution/spec/api/#chunked-upload.

Le kubeconfig conteneurisé conserve la vérification TLS,
avec adresse du nœud kind et tls-server-name ; aucun insecure-skip-tls-verify.
Rollout puis readiness et rôles HTTP sont vérifiés depuis le contrôleur.

## Monitoring

Terraform gère les six ressources Prometheus déjà appliquées avec accord et sans
dérive observée. Grafana est un manifeste séparé dans events-grafana, avec PVC,
image épinglée, utilisateur 472 et limite 256 Mio. Son datasource est provisionné
vers le Service Prometheus ; quatre requêtes réelles sont contrôlées.

```powershell
python scripts/Initialize-LocalGrafana.py
powershell -File scripts/Start-LocalForwards.ps1 -IncludeGrafana
python scripts/Verify-Grafana.py
python scripts/Capture-Grafana.py
```

Après un rollout, relancer les forwards avec -Restart : un port-forward existant
peut rester attaché à l'ancien pod. URL Grafana : http://localhost:13000.
