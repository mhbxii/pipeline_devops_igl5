# Plan incrémental

| Phase | Difficulté | Acceptation |
|---|---|---|
| 1 Socle et preuves | Moyenne | Compilation, connexion DB, API réelle |
| 2 Rôles Tache, logs, tests | Moyenne | Affecter/lire/modifier/retirer un rôle, erreurs vérifiées |
| 3 Maven et Git | Faible | Cycle Maven sans ignorer les tests, commits explicites |
| 4 Docker | Moyenne | Image construite, API et DB accessibles |
| 5 Sonar/Nexus | Moyenne | Analyse réelle et artefact publié après autorisation |
| 6 Jenkins | Élevée | Pipeline réel avec gate et vérification de déploiement |
| 7 Kubernetes | Élevée | Probes, pods prêts et API fonctionnelle |
| 8 Terraform | Moyenne | fmt/init/validate/plan puis apply approuvé |
| 9 Monitoring et soutenance | Moyenne | Métriques réelles et preuves présentables |

Chaque phase produit documentation, preuve, explication française et questions orales.
Les phases 4–9 dépendent du socle et des capacités de la machine.
