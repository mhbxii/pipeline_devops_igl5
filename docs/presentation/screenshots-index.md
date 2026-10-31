# Index des preuves de la soutenance actuelle

Fichier courant : eventsProject-Tache-Soutenance.pptx ; sources : slides-ready.json.
Les captures AWS historiques sont explicitement datées du 29 octobre sur chaque slide.

| Slide | Sujet | Nature | Preuve |
| --- | --- | --- | --- |
| 1 | eventsProject | Vérifié au 31/10 | État vérifié le 31 octobre 2026 • Source testée : 8a5a96f |
| 2 | Un périmètre métier précis | Vérifié au 31/10 | entities/Tache.java • services/TacheService.java |
| 3 | Du code au déploiement EKS | Vérifié au 31/10 | Jenkinsfile • Étapes AWS désactivées dans le build #15 |
| 4 | Persister un rôle, conserver le modèle | Vérifié au 31/10 | entities/Tache.java • entities/Participant.java |
| 5 | Un cycle de rôle vérifié par HTTP | Vérifié au 31/10 | 16-readiness/final-baseline-readiness.json |
| 6 | Limiter les champs reçus par l'API | Vérifié au 31/10 | controllers/dto/RoleRequest.java • EventRequests.java |
| 7 | Des réponses sans graphe JPA récursif | Vérifié au 31/10 | Exemple de JSON conforme au DTO • EventResponses.java • docs/DTO_API.md |
| 8 | Rejeter les erreurs avant la persistance | Vérifié au 31/10 | EventReferenceValidationTest • TacheRestControllerTest • HTTP/MySQL réel |
| 9 | 52 tests automatisés réussis | Vérifié au 31/10 | [Capture authentique](../evidence/16-readiness/junit-build-15.png) |
| 10 | JaCoCo : une couverture mesurée | Vérifié au 31/10 | [Capture authentique](../evidence/16-readiness/jacoco-build-15.png) |
| 11 | Un quality gate réellement évalué | Vérifié au 31/10 | 16-readiness/sonar-build-15.json |
| 12 | Tracer les changements sur Tache | Vérifié au 31/10 | Jalons Git, résumés des commits • Source du build #15 |
| 13 | Un seul pipeline, une release actuelle | Vérifié au 31/10 | 08-jenkins/build-15-summary.json • Jenkinsfile |
| 14 | Un plan avant toute création AWS | Reprise AWS à effectuer | terraform/aws-eks/ • docs/JENKINS_TERRAFORM.md |
| 15 | Le même JAR dans toute la chaîne | Vérifié au 31/10 | 16-readiness/release-15-verification.json |
| 16 | Une image publique vérifiée | Vérifié au 31/10 | Digest : 5c4ff54095b9ae76… • 16-readiness/release-15-verification.json |
| 17 | Tester l'image réellement construite | Vérifié au 31/10 | 16-readiness/docker-image-15-smoke.json |
| 18 | Des logs et des métriques exploitables | Vérifié au 31/10 | application.properties • TacheService.java • TacheExceptionHandler.java |
| 19 | Une infrastructure de démonstration maîtrisée | Reprise AWS à effectuer | terraform/aws-eks/ • k8s/eks/ • Création actuelle en attente de revue |
| 20 | EKS : démonstration réalisée le 29 octobre | Historique du 29/10 | [Capture authentique](../evidence/14-aws-eks/resources.png) |
| 21 | Prometheus : une cible réellement UP | Historique du 29/10 | [Capture authentique](../evidence/14-aws-eks/prometheus-targets.png) |
| 22 | Grafana : un dashboard provisionné | Historique du 29/10 | [Capture authentique](../evidence/14-aws-eks/grafana-dashboard.png) |
| 23 | Nettoyer après la démonstration | Historique du 29/10 | [Capture authentique](../evidence/14-aws-eks/cleanup-verification.png) |
| 24 | Les étapes pour relancer la démonstration | Reprise AWS à effectuer | docs/AWS_EKS_OPERATIONS.md • docs/JENKINS_TERRAFORM.md |
| 25 | Des résultats vérifiés, une reprise documentée | Vérifié au 31/10 | docs/READINESS.md • docs/EVALUATION_TRACKER.md |
| 26 | Trois choix à expliquer | Vérifié au 31/10 | docs/ORAL_DEFENSE.md • docs/DTO_API.md |
