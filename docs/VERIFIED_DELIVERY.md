# Livraison vérifiée — 29 octobre 2026

Branche Tache ; affectation des rôles autour de l'enum existant.

## Application et tests
- 38 tests passent : service Mockito, MVC, JPA/H2 et mapping DTO.
- Quatre anciennes entrées EventRestController remplacées par des DTO après accord
  utilisateur ; identifiants générés et coût calculé exclus des champs modifiables.
- Compatibilité des quatre routes vérifiée par HTTP contre MySQL dans Kubernetes.
- Log4j2 et JaCoCo actifs ; TacheService intégralement couvert.

## CI/CD vérifiée
- Jenkins 2.580.2 : build #4 SUCCESS, commit 05fae5705048. Compilation, 38 tests,
  Sonar, gate, package, publication Nexus, image, chargement kind, rollout et HTTP.
- Sonar : zéro vulnérabilité, bug, code smell ou hotspot ; couverture globale 49,6 %,
  nouveau code 81,9 %. Gate OK avec conditions évaluées, webhook HTTP 200 signé.
- L'analyse initiale avait quatre vulnérabilités et un gate aux conditions vides :
  ces preuves historiques ne représentent pas le résultat final.
- Build #1 : erreur de plugin corrigée ; #2 : livraison locale initiale réussie ;
  #3 : arrêté après blocage du callback privé Sonar ; #4 : chaîne locale complète.
- Nexus snapshot 1.0.0-20261029.104207-2 ; JAR Jenkins et Nexus de SHA-256 identique :
  00d73ef40c0b59971f5e250aa455dcc968fc54a167386750f8466f8b2fb32ccd.
- Image déployée : events-tache:tache-05fae5705048-4. Les tags incluent commit et
  numéro de build ; contrôle du JAR dans le pod ajouté au vérificateur.

## Infrastructure et prochaines étapes
- kind events-tache, Kubernetes 1.32.2 ; application/MySQL avec probes et PVC.
- Terraform : six ressources Prometheus appliquées après revue et accord ; plan
  suivant sans différence. Prometheus collecte events avec up=1.
- Grafana 13.3.0 vérifié dans events-grafana : base OK, datasource sain et quatre
  requêtes non vides ; screenshot du dashboard réellement rendu et vérifié.
- Publication publique autorisée vers ayyoub1/events-tache. Build #5 : tests, gate,
  Nexus et image réussis ; publication échouée sur fermeture réseau du proxy Docker.
  Aucun rollout n'a suivi cet échec ; build #4 reste le déploiement validé.
  Contournement par upload de blobs OCI en morceaux en cours, sans succès revendiqué.
- Aucun push Git distant réalisé. Commits réels par jalon, aucune date falsifiée.
- Docker Desktop relancé sans effacement ; environ 25 Gio libres sur C:.
- Présentation actualisée : 24 slides françaises, descriptions visibles, sans notes,
  captures Jenkins/Sonar/Grafana et triple SHA-256 ; export et contenu XML validés.
- Suite : résoudre le transport du registre, publier via Jenkins puis vérifier
  Docker Hub et le déploiement de l'image publique.

## Particularités du laboratoire
Sonar utilise H2 pour cette démonstration locale. Pour le callback vers le réseau
Docker privé, sonar.validateWebhooks=false est global à cette instance personnelle ;
la signature HMAC reste vérifiée. Ce réglage n'est pas une liste d'hôtes autorisés.
Jenkins contrôle le socket Docker : réservé aux jobs de confiance sur ce poste.
Terraform gère Prometheus uniquement ; Grafana utilise un manifeste séparé.
