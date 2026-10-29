# Exploitation du laboratoire EKS

Région : us-east-1. Cluster dédié : events-tache-ayyoub. Profil : events-tache-lab.
Le plan et l'estimation figurent dans AWS_EKS_PLAN.md ; le résultat effectif est
suivi dans CURRENT_STATE.md. Ne pas confondre manifests préparés et succès vérifié.

## Après création de l'infrastructure approuvée

Depuis la racine du dépôt :

```powershell
aws eks update-kubeconfig --profile events-tache-lab --region us-east-1 --name events-tache-ayyoub --alias events-tache-eks --kubeconfig .local/kubeconfig-eks
.local/tools/kubectl-eks.exe --kubeconfig .local/kubeconfig-eks --context events-tache-eks get nodes
python scripts/Deploy-Eks.py
./scripts/Start-LocalForwards.ps1 -Eks -IncludeGrafana
./scripts/Test-TacheApi.ps1 -BaseUrl http://localhost:18189/events
python scripts/Verify-Eks.py
python scripts/Capture-Grafana.py --eks
./scripts/Capture-BaselineEvidence.ps1 -EksOnly
```

Les mots de passe spécifiques EKS sont générés dans .env ignoré. Les Secrets sont
envoyés en mémoire sur stdin ; aucun fichier de manifests contenant les valeurs
n'est produit. Le script exige le contexte EKS dédié. Les contexts kind et EKS
utilisent des fichiers séparés ; aucune modification du contexte Kubernetes global.

| Service EKS via tunnel local | Adresse |
| --- | --- |
| API | http://localhost:18189/events/taches |
| Prometheus | http://localhost:19190/targets |
| Grafana | http://localhost:13100 |

Grafana : utilisateur tache-admin, secret EKS_GRAFANA_ADMIN_PASSWORD dans .env.
Ne pas afficher ce secret dans une capture, un terminal enregistré ou une diapositive.
Les adresses ci-dessus fonctionnent seulement tant que les port-forwards sont actifs.
L'API AWS n'accepte que l'IPv4 /32 examinée. Un changement de sortie réseau demande
une nouvelle revue du plan Terraform ; ne pas ouvrir 0.0.0.0/0 par commodité.

## Livraison Jenkins vers AWS

`ci/Jenkinsfile.eks` définit un job distinct de promotion, `tache-eks-delivery`.
Il contrôle que tache-ci #9 est SUCCESS, puis déploie le manifeste Docker public
vérifié par digest et contrôle readiness/rôles par HTTP depuis Jenkins.
Il ne recompile pas cette release et ne lance pas Terraform apply.
Le pipeline CI principal conserve son historique et sa destination kind.

Le binaire kubectl Linux 1.36.6 est téléchargé dans .local/tools/ et vérifié par
SHA-256, puis copié dans le volume Jenkins. Après disponibilité des workloads :

```powershell
python scripts/Configure-EksJenkins.py
```

Ce script configure le job, sans le lancer. Il crée un token Kubernetes temporaire
de deux heures pour le ServiceAccount jenkins-deployer. Le rôle autorise seulement
lecture/patch/watch du Deployment events, lecture des pods et du Service events,
et création de tunnels port-forward dans events-tache. Aucun accès Secrets ou IAM.
Le credential Jenkins est un Secret file, utilisé via withCredentials.
Les jobs Jenkins restent réservés aux auteurs de confiance : le daemon Docker est
privilégié dans ce laboratoire local. Le token court ne remplace pas cette limite.
Renouveler ce credential pendant une session active avant une nouvelle promotion.

## Fin de démonstration et maîtrise des frais

Exporter d'abord les preuves, puis obtenir l'accord pour le nettoyage destructif.
Les volumes dynamiques sont hors state Terraform AWS : les PVC doivent être
supprimés et les volumes EBS contrôlés pendant que le worker et EBS CSI fonctionnent.
Sinon, la suppression du cluster peut laisser du stockage facturé et des finalizers.
Ne jamais supprimer le VPC par défaut, ses subnets ou LabRole.

Après le nettoyage Kubernetes approuvé, préparer le plan de destruction :

```powershell
.local/tools/terraform.exe '-chdir=terraform/aws-eks' plan -destroy -input=false '-var-file=../../.local/aws-eks.tfvars.json' '-out=../../.local/aws-eks-destroy.tfplan'
```

Examiner ce nouveau plan et demander l'accord avant de l'appliquer. Conserver le
state local jusqu'à vérification de la suppression du cluster, worker et volumes.
La fermeture d'une session Learner Lab n'est pas une preuve de suppression EKS.
