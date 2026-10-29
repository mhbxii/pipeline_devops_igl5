# EKS de démonstration — plan à approuver

Root et state séparés de terraform/ (Prometheus local). Ne pas déplacer les states.
Profil local : events-tache-lab, région us-east-1, compte contrôlé par allowed_account_ids.
Variables privées : .local/aws-eks.tfvars.json ; aucun credential dans Terraform.

Le plan prévoit quatre ressources gérées : cluster EKS 1.36, launch template,
node group fixe d'un t3.medium, add-on EBS CSI. Le VPC, deux subnets et LabRole
existants sont des data sources et restent hors du state géré.
AWS crée aussi les dépendances du service : instance, volume système, ENI, groupes
de sécurité et éventuellement ses service-linked roles absents au préflight.

## Choix du laboratoire
- Un worker, 2 vCPU et 4 Gio, une seule AZ ; démonstration sans haute disponibilité.
- Control plane en deux AZ, EKS 1.36 en support standard ; pas de tarif extended.
- Pas de NAT gateway, load balancer, SSH ni règle NodePort ouverte à Internet.
- API EKS publique limitée aux IPv4 /32 de l'opérateur et de Jenkins ; endpoint privé
  activé pour les nœuds. API applicative accessible par port-forward authentifié.
- Disque système gp3 chiffré 20 Gio, supprimé avec l'instance. CPU credits standard
  pour éviter les suppléments Unlimited.
- EBS CSI 1.67.2 et un contrôleur : permissions EC2 du LabRole vérifiées par simulation.
  IMDSv2 avec hop limit 2 permet au contrôleur de récupérer le rôle du worker.
  Cette réutilisation du rôle large du lab concerne un cluster de confiance personnel ;
  les rôles dédiés par service sont préférables hors de ce laboratoire restreint.
- Les PVC applicatifs seront déployés après validation du cluster/driver : gp3,
  WaitForFirstConsumer, stockage MySQL et monitoring. Leur coût est inclus dans
  l'estimation globale mais ils ne figurent pas dans ce premier plan infrastructure.

## Reproduction du plan depuis la racine du dépôt
```powershell
.local/tools/terraform.exe -chdir=terraform/aws-eks init -input=false
.local/tools/terraform.exe -chdir=terraform/aws-eks validate
.local/tools/terraform.exe -chdir=terraform/aws-eks plan -input=false '-var-file=../../.local/aws-eks.tfvars.json' '-out=../../.local/aws-eks.tfplan'
```

Ne pas exécuter apply avant revue du plan sauvegardé et accord utilisateur.
Ne pas utiliser terraform apply -auto-approve sur la configuration non sauvegardée.
Après la démo, supprimer les PVC de test et vérifier les volumes associés avant
un plan de destruction à examiner et approuver ; ne pas présumer que End Lab arrête
tous les frais EKS ou supprime le stockage.
