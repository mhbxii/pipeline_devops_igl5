# Etat courant ? 2026-10-29

MySQL corrige : Compose events-tache, port 3307, compte events, secrets dans .env ignore.
Verification HTTP/MySQL reelle : scripts/Test-TacheApi.ps1, cycle complet PASS,
reponses 200/204, validation 400, absent 404 ; ancien getLogs retourne 200.
Code ajoute : TacheService, TacheRestController, TacheExceptionHandler.
18 tests reussis : Mockito, MVC, integration JPA/H2 sans mocks. JaCoCo genere.
TacheService : 24 lignes couvertes, 0 manquee ; 4 branches couvertes, 0 manquee
au premier rapport (couverture du service, pas du projet entier).
Log4j2 configure ; INFO/DEBUG/WARN observes sur API, ERROR sur test de panne simulee.
Presentation : preference utilisateur = descriptions visibles, aucune note orateur.
Deck phase-01 regenere sans notes. Extension avec nouveaux resultats en cours.

Cycle Maven clean/compile/test/package/install en cours ; voir journaux 02-maven.
Dockerfile et service app prepares ; image pas encore verifiee.
Utilisateur autorise le telechargement local de kind et Terraform dans .local/tools.
Telechargements et verifications SHA-256 en cours. Aucun Terraform apply autorise.
Prochaines actions : finir Maven, construire/executer Docker, capturer preuves,
creer un cluster kind local si ressources suffisantes puis verifier Kubernetes.
Ne pas publier a distance sans autorisation ; ne pas afficher .env ni compose config.
