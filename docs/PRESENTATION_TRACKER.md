# Presentation verifiee

Livrable : presentation/eventsProject-Tache-GitHub.pptx ; source slides.json.
26 diapositives, descriptions francaises visibles, aucune note orateur.
Capture authentique Windows et Edge ; validation XML et export des 26 diapositives.
Jenkins #9, 38 tests, gate Sonar, DTO historiques, Nexus, Docker, Kubernetes,
Terraform, Prometheus, Grafana et triple SHA-256 inclus.
Docker Hub public et deploiement verifies : build #9 automatique SUCCESS, capture du vrai tag.
Le fichier phase-01 reste un instantane historique.

Index detaille : presentation/screenshots-index.md.
Scripts : Capture-BaselineEvidence.ps1, Capture-ServiceDashboards.py,
Capture-CiDashboards.py, Capture-Grafana.py, Build-BaselinePresentation.ps1,
Validate-Presentation.py.

Nouvelle slide GitHub : cause SCMTriggerCause, URL et commit du build #9.
Le fichier de 25 slides etant ouvert/verrouille, il est conserve.
Generer la version GitHub avec presentation/Build-GitHubPresentation.ps1 ;
valider avec presentation/Validate-GitHubPresentation.py (generateurs partages).
