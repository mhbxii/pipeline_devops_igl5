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

Le pipeline unique est désormais Jenkinsfile, job tache-ci : compilation, tests,
Sonar, Terraform AWS, publications et livraison de l'image du build courant sur EKS.
L'ancien ci/Jenkinsfile.eks est supprimé ; les scripts de configuration désactivent
le job de promotion et conservent son historique. Voir JENKINS_TERRAFORM.md.
Ces modifications locales ne sont pas encore une nouvelle démonstration AWS.

Après bootstrap, `python scripts/Configure-EksJenkins.py` renouvelle le Secret file
`eks-kubeconfig` pour tache-ci, sans créer de job. Le token de deux heures autorise
lecture/patch/watch du Deployment events, lecture des pods et du Service events,
et port-forward dans events-tache. Aucun accès Secrets ou IAM.
Les credentials Terraform AWS sont distincts et temporaires. Le bootstrap des
workloads reste opérateur et approuvé : le token limité ne peut créer ces ressources.
Les jobs Jenkins sont réservés aux auteurs de confiance.

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

Pour le nettoyage explicitement approuvé de cette démonstration, le script protège
le compte, le contexte, le hash du plan et l'identité des trois volumes avant de
supprimer les namespaces. Il attend l'absence des volumes avant l'apply du plan :

```powershell
python scripts/Remove-EksDemo.py --approved-plan-sha256 6f7d712fb3dcad774c4f35280f57759c47b83698ee0ec296269dcd617cb5c8eb
python scripts/Verify-EksCleanup.py
```

Cette commande est destructive, réservée au plan examiné et déjà approuvé. Ne pas
la réexécuter après destruction. Un futur déploiement demandera un nouveau plan,
une nouvelle revue et un accord. La vérification est en lecture seule : cluster,
EC2, ASG, launch template, volumes, interfaces réseau, security group et state.
Le job EKS est désactivé après la démo et son credential temporaire supprimé ;
Configure-EksJenkins.py renouvelle uniquement le credential de tache-ci après recréation.
