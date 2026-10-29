# Projet : état courant — 2026-10-29

Branche Tache. Périmètre confirmé : affectation des rôles autour de l'enum existant.

## Vérifié
- MySQL : base dédiée, port 3307, compte events, secrets .env ignorés par Git.
- Tache API : consultation, affectation/remplacement, retrait, validations et erreurs HTTP.
- 20 tests passent : Mockito, MVC et intégration JPA/H2 sans mocks.
- Maven clean/compile/test/package/install/verify réussis ; aucun test masqué.
- Log4j2 actif ; INFO/DEBUG/WARN réels, ERROR démontré par test de panne simulée.
- TacheService : 24 lignes et 4 branches couvertes au rapport JaCoCo.
- Docker : cycle HTTP/MySQL complet, utilisateur non privilégié, readiness UP.
- Kubernetes kind 1.32.2 : app/MySQL prêts, PVC lié, cycle HTTP complet via Service.
- Terraform : 6 ressources Prometheus appliquées après revue et accord utilisateur ;
  plan suivant sans différence. Prometheus up{job="events"}=1.
- Nexus : snapshot publié, JAR téléchargé et SHA-256 identique au livrable local.
  EULA acceptée après accord explicite. Compte de publication dédié, image épinglée par digest.

## Blocage actuel
C: environ 1 Gio libre ; builds/téléchargements Jenkins/Sonar interrompus.
Nettoyage autorisé : 31,91 Go de cache Docker inutilisé supprimé ; archives temporaires
linux/amd64 supprimées. fstrim a signalé 20,4 Gio sur le disque de données.
Le VHDX Docker reste à 57 600 376 832 octets et Windows ne récupère pas encore l'espace.
L'agent n'est pas administrateur. Aucun arrêt global WSL ni compactage exécuté.
Helper scripts/Compact-DockerDisk.ps1 préparé et parsé sans erreur, non exécuté.
Il demande PowerShell administrateur et -AllowWslShutdown : sauvegarder les travaux WSL.
Prochaine action : intervention de compactage, puis reprise Jenkins/Sonar.
Jenkinsfile et bootstrap authentifié prêts ; aucun build Jenkins ou gate Sonar validé.
Grafana et publication Docker Hub restent à réaliser. Aucun push distant autorisé/exécuté.

## Services et reprise
Compose app/mysql arrêtés après validation ; cluster kind et Nexus actifs.
Port-forwards de session : application 18089, Prometheus 19090 ; relancer au besoin.
Kubeconfig : .local/kubeconfig. Outils : .local/tools (kind 0.33.0, Terraform 1.17.1,
checksums officiels vérifiés, téléchargements autorisés). State et secrets ignorés.
Terraform gère uniquement Prometheus et son namespace, pas le cluster ni l'application.
Script Publish-LocalNexus.ps1 fonctionne ; script Initialize-LocalJenkins.ps1 attend l'espace disque.

## Documentation et Git
Présentation courante : docs/presentation/eventsProject-Tache.pptx.
Descriptions françaises visibles, aucune note orateur ; source slides.json.
Ancien phase-01 peut être ouvert/verrouillé : conserver ce fichier historique.
Captures Windows réelles et Edge/Playwright automatisées ; preuves dans docs/evidence/.
Commits réels par jalon, dates réelles. Aucune planification de commits nocturnes.
Lire EVALUATION_TRACKER.md pour les statuts détaillés ; ne pas revendiquer la CI complète.
