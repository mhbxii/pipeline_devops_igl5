# GitHub vers Jenkins — migration en cours

SCM : https://github.com/mhbxii/pipeline_devops_igl5.git, branche */Tache.
Script : Jenkinsfile. Polling : H/5 * * * *, donc toutes les cinq minutes.
Le dépôt public est lisible sans credentials depuis le conteneur Jenkins.

Le job a été désactivé pendant la migration puis réactivé. Le bootstrap est mis
à jour, l'image Jenkins reconstruite et le contrôleur recréé avec ses volumes.
Le montage du miroir local et ALLOW_LOCAL_CHECKOUT ont été supprimés.
Le premier poll conservait aussi le SCM local du dernier build historique.
Build #8, lancé manuellement comme baseline : SUCCESS, checkout GitHub du commit
02fe596, 38 tests et gate OK ; aucune publication ni modification du déploiement.
Il établit l'historique SCM GitHub pour les polls suivants. La preuve automatique
sera enregistrée séparément après le prochain commit source.

Les paramètres par défaut activent Sonar, Nexus, Docker Hub et rollout Kubernetes.
Destination publique approuvée : ayyoub1/events-tache. Aucun autre dépôt publié.
Les commits limités à docs/ ne déclenchent pas de build ; toute autre modification
est éligible. Jenkins et Docker Desktop doivent fonctionner pour que le polling ait lieu.

## Vérification prévue
1. Pousser le commit de configuration sur Tache.
2. Exécuter Configure-GitHubScm.py --enable.
3. Laisser le timer SCM détecter la modification ; ne pas cliquer Build Now.
4. Observer avec Verify-GitHubTrigger.py --after-build 8 --expected-commit SHA.
5. Enregistrer cause SCMTriggerCause, URL GitHub, commit, polling log et résultat.
6. Vérifier la livraison, puis actualiser les captures et slides.

Sources officielles :
- https://www.jenkins.io/doc/book/pipeline/syntax/#triggers
- https://www.jenkins.io/doc/pipeline/steps/params/fromscm/ (PathRestriction)
