# Architecture constatée

Spring Boot 2.7.6 / Java cible 8 / Maven / Spring MVC / Spring Data JPA / MySQL.
Package : src/main/java/tn/esprit/eventsproject.
EventRestController -> IEventServices / EventServicesImpl -> trois JpaRepository.
Entités persistantes : Participant, Event, Logistics. Tache est un enum stocké
en chaîne dans Participant.tache. Aucun DTO, service ou repository Tache autonome.
Le calcul planifié des coûts filtre les participants ORGANISATEUR.
Logging actuel : SLF4J via Lombok, backend Log4j2 (Logback exclu du POM).
PerformanceAspect mesure les appels services. Application : port 8089, contexte /events.
Base configurée : eventsProject sur localhost:3306, utilisateur root, mot de passe vide.

Contribution ajoutée : TacheRestController -> TacheService -> ParticipantRepository.
DTO limité à participantId/tache ; transactions et erreurs HTTP limitées à ce module.
Profil local -> MySQL Compose sur 3307 ; déploiement Docker vérifié avec runtime Java 17.
Kubernetes kind 1.32.2 : namespace events-tache, application et MySQL prêts, PVC MySQL.
Terraform : namespace events-monitoring, quota, ConfigMap, PVC, Deployment et Service
Prometheus. Apply approuvé et vérifié ; scrape Actuator UP.
Jenkins/SonarQube/Nexus : configuration préparée, images en préparation ; chaîne intégrée
encore à vérifier. Grafana reste à réaliser.

```mermaid
flowchart LR
    API[Tache REST] --> Service[TacheService]
    Service --> Repo[ParticipantRepository]
    Repo --> MySQL[(MySQL)]
    Prom[Prometheus géré par Terraform] --> Metrics[Actuator /prometheus]
    Metrics --> API
```
