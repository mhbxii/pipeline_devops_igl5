# Soutenance actuelle — 31 octobre 2026

Présentation à utiliser : [eventsProject-Tache-Soutenance.pptx](eventsProject-Tache-Soutenance.pptx).
Version à partager/imprimer : [PDF](eventsProject-Tache-Soutenance.pdf).

26 diapositives françaises, descriptions visibles, aucune note orateur.
Jenkins #15 : 52 tests, gate OK, publications Nexus/Docker et intégrité vérifiées.
Les slides 20 à 23 montrent la démonstration AWS du 29 octobre, ensuite nettoyée.
La nouvelle activation EKS est en attente du Learner Lab ; aucune disponibilité actuelle inventée.

Les anciennes présentations GitHub et AWS-EKS sont conservées comme archives.

Générer : powershell -File docs/presentation/Build-ReadyPresentation.ps1
Valider : python docs/presentation/Validate-ReadyPresentation.py

## Parcours

1. eventsProject
2. Un périmètre métier précis
3. Du code au déploiement EKS
4. Persister un rôle, conserver le modèle
5. Un cycle de rôle vérifié par HTTP
6. Limiter les champs reçus par l'API
7. Des réponses sans graphe JPA récursif
8. Rejeter les erreurs avant la persistance
9. 52 tests automatisés réussis
10. JaCoCo : une couverture mesurée
11. Un quality gate réellement évalué
12. Tracer les changements sur Tache
13. Un seul pipeline, une release actuelle
14. Un plan avant toute création AWS
15. Le même JAR dans toute la chaîne
16. Une image publique vérifiée
17. Tester l'image réellement construite
18. Des logs et des métriques exploitables
19. Une infrastructure de démonstration maîtrisée
20. EKS : démonstration réalisée le 29 octobre
21. Prometheus : une cible réellement UP
22. Grafana : un dashboard provisionné
23. Nettoyer après la démonstration
24. Les étapes pour relancer la démonstration
25. Des résultats vérifiés, une reprise documentée
26. Trois choix à expliquer
