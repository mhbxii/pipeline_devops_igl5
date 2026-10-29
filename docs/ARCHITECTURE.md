# Architecture constatée

Spring Boot 2.7.6 / Java cible 8 / Maven / Spring MVC / Spring Data JPA / MySQL.
Package : src/main/java/tn/esprit/eventsproject.
EventRestController -> IEventServices / EventServicesImpl -> trois JpaRepository.
Entités persistantes : Participant, Event, Logistics. Tache est un enum stocké
en chaîne dans Participant.tache. Aucun DTO, service ou repository Tache autonome.
Le calcul planifié des coûts filtre les participants ORGANISATEUR.
Logging actuel : SLF4J via Lombok, backend Spring Boot par défaut (Logback).
PerformanceAspect mesure les appels services. Application : port 8089, contexte /events.
Base configurée : eventsProject sur localhost:3306, utilisateur root, mot de passe vide.

Cible envisagée, non déployée : Git -> Jenkins -> Maven/JUnit -> SonarQube -> Nexus
-> image Docker -> Kubernetes local. Terraform gère des ressources Kubernetes dédiées.
Prometheus/Grafana après validation des exigences principales et ressources disponibles.
