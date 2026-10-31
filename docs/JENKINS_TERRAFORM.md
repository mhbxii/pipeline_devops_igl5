# Pipeline unique Jenkins vers AWS EKS

Modification du 30 octobre 2026, branche Tache. Le pipeline unique est
`Jenkinsfile`, job `tache-ci`. `ci/Jenkinsfile.eks` est supprimé. Les scripts de
configuration désactivent l'ancien job sans effacer son historique. Sources publiées,
Jenkins reconstruit et outils vérifiés. Jenkins #15 SUCCESS pour CI/publication ;
activation EKS encore bloquée par l'accès au Learner Lab, sans nouvel apply.

## Parcours et défense des sources

1. Checkout, compilation, tests, couverture, SonarQube et quality gate.
2. Terraform fmt/init/validate/plan sur `terraform/aws-eks`.
3. Apply optionnel du plan exact, après revue des coûts et approbation Jenkins
   par tache-admin ; délai d'approbation de dix minutes.
4. Vérification des accès au Deployment et Service EKS existants.
5. Package, publication Nexus, construction et publication Docker Hub.
6. Déploiement de l'image du build courant sur events-tache-eks, rollout et
   vérifications HTTP readiness/rôles.

Les étapes kind et leurs paramètres sont supprimés. La livraison ne dépend plus
du build #9. Jenkins, Sonar et Nexus restent locaux ; Kubernetes cible EKS.
L'image Jenkins prépare Terraform 1.17.1 et kubectl 1.36.6 avec checksum SHA-256.

## Paramètres, state et approbation

RUN_TERRAFORM et DEPLOY_EKS sont activés par défaut. APPLY_TERRAFORM est désactivé.
Un plan avec différences bloque la livraison si l'apply n'est pas demandé.
Pour une revue de plan, désactiver DEPLOY_EKS, PUBLISH_IMAGE et PUBLISH_NEXUS.
Pour une CI sans AWS, désactiver RUN_TERRAFORM et DEPLOY_EKS.
L'apply exige une approbation dans le build qui a produit le plan.

Le backend local pointe sur `/terraform-eks-state/terraform.tfstate`, montage du
répertoire hôte `terraform/aws-eks`. Le state canonique AWS est réutilisé ; celui
du monitoring kind reste intact. Un state absent bloque le build. Le state vide
après destruction est valide pour préparer un nouveau plan de création.
Le code provient du checkout, pas du répertoire monté.
Ne pas exécuter Terraform simultanément depuis Windows et Jenkins : le verrou
local sur Docker Desktop n'est pas un verrou distribué. Pour plusieurs agents,
prévoir un backend distant verrouillé avec une migration séparément approuvée.

Le plan reste dans un répertoire privé hors workspace, supprimé par post/always,
sans archivage. Une panne brutale peut nécessiter un nettoyage manuel.
Durée maximale du build : 90 minutes pour permettre la création EKS.

## Credentials Jenkins

| ID | Type | Contenu |
| --- | --- | --- |
| aws-lab-credentials | Secret file | INI avec le seul profil events-tache-lab : aws_access_key_id, aws_secret_access_key, aws_session_token |
| aws-eks-tfvars | Secret file | JSON privé : compte attendu, région, cluster et CIDRs IPv4 /32 examinés |
| eks-kubeconfig | Secret file | Token de livraison Kubernetes limité au namespace, valide deux heures |
| sonar-token / sonar-webhook | Secret text | Analyse et callback existants |
| nexus / dockerhub | Username/password | Publications existantes |

Le profil AWS est forcé à events-tache-lab ; allowed_account_ids protège le compte.
Renouveler les credentials avant plan/apply ; ils doivent rester valides pendant
l'opération. Ne pas fournir le profil personnel par défaut.
Le fichier `.local/aws-eks.tfvars.json` fournit les variables privées, à revoir
notamment si l'IPv4 de sortie Jenkins a changé. Aucun secret dans Git.

## Activation et première création

L'ancien cluster a été supprimé. Aucune nouvelle exécution EKS n'est revendiquée.

1. Après accord pour l'installation, reconstruire Jenkins :
   `docker compose -f docker-compose.devops.yml --profile jenkins up -d --build jenkins`.
2. Après accord de publication, publier sur Tache et exécuter
   `python scripts/Configure-GitHubScm.py`. L'ancien job est désactivé avec son
   historique conservé. Le premier build actualise les paramètres.
3. Renouveler la session lab et configurer les deux Secret files Terraform.
   `python scripts/Configure-AwsTerraformJenkins.py` vérifie STS et le compte
   attendu, contrôle l'accès EKS et actualise les sorties IPv4 /32 du poste et
   de Jenkins, puis copie uniquement le profil dédié et les variables privées.
4. Exécuter un build avec DEPLOY_EKS=false et publications désactivées pour revoir
   le plan. Un build avec APPLY_TERRAFORM=true demande confirmation du plan qu'il
   vient de produire ; ne confirmer qu'après revue.
5. Après création approuvée, préparer les workloads via AWS_EKS_OPERATIONS.md :
   kubeconfig, Deploy-Eks.py, puis Configure-EksJenkins.py. Ce dernier renouvelle
   le credential de tache-ci ; il ne crée plus de pipeline ni n'installe d'outil.
6. Activer DEPLOY_EKS : le même pipeline livre l'image du build courant.

Le bootstrap des namespaces, Secrets, MySQL, supervision et RBAC reste une
préparation opérateur après accord. Le token de livraison limité ne peut créer
ces ressources. Le nettoyage AWS reste séparé et approuvé.

## Références

Validation locale : Jenkinsfile accepté par le validateur déclaratif Jenkins ;
module Terraform AWS fmt/validate, Compose, syntaxe Python et shell vérifiés.
Aucun nouveau build de livraison, plan AWS ou apply exécuté pendant cette fusion.

[Backend local Terraform](https://developer.hashicorp.com/terraform/language/backend/local)
et [plan sauvegardé](https://developer.hashicorp.com/terraform/cli/commands/plan).
