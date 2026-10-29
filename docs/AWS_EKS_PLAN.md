# Revue EKS — Learner Lab personnel

Préparation du 29 octobre 2026, région us-east-1. Budget restant déclaré : 50 USD.
Compte dédié à l'utilisateur. Fin de session annoncée : 23 h 16, heure de Tunis.
Le plan exact a été approuvé par l'utilisateur, ainsi que kubectl local 1.36.6.
Apply terminé avec succès : 4 added, 0 changed, 0 destroyed. Worker Ready et EBS CSI
prêt. Preuve expurgée : docs/evidence/14-aws-eks/terraform-apply.txt.
Les workloads ont ensuite été vérifiés (API, persistance MySQL et monitoring),
puis le nettoyage a été approuvé et exécuté. Le cluster est désormais supprimé ;
voir AWS_EKS_CLEANUP_REVIEW.md et evidence/14-aws-eks/cleanup-verification.json.

## Contrôles effectués en lecture seule

Identité du profil events-tache-lab vérifiée dans le compte attendu. Les credentials
temporaires restent dans le fichier AWS de l'utilisateur, hors Git et preuves.
Le VPC par défaut possède deux subnets publics utilisables dans us-east-1a et 1b,
avec route Internet et attribution IPv4. Aucun cluster EKS existant dans la région.
LabRole existe et accepte EKS et EC2. La simulation IAM autorise CreateCluster,
CreateNodegroup, CreateAddon, PassRole contextualisé, la création des service-linked
roles EKS et les opérations EBS nécessaires. Une simulation ne garantit pas les
quotas ou l'absence de restrictions supplémentaires lors de l'exécution.

L'API EKS annonce 1.36 en support standard jusqu'au 2 août 2027 et propose
l'add-on EBS CSI v1.67.2-eksbuild.1. L'IPv4 de sortie de Jenkins est identique à celle
du poste ; elle reste dans les variables locales ignorées et limite l'API à /32.

## Périmètre proposé

Root Terraform séparé : terraform/aws-eks/. Le state Prometheus local ne change pas.
Plan sauvegardé et inspecté : **4 créations, 0 modification, 0 suppression**.
Cluster events-tache-ayyoub, launch template, node group fixe d'un t3.medium et
add-on EBS CSI. Les data sources existantes sont seulement lues.
Terraform validate : succès ; plan : exit 0, applyable=true, complete=true.
Fichier privé : .local/aws-eks.tfplan ; SHA-256 :
`9ed4328c282aa07b41181184be73f520d8d21554df4e1a92a2cc7ae8b627eff5`.
Provider AWS verrouillé : 6.71.0, signé HashiCorp.

Le worker dispose de 2 vCPU, 4 Gio, disque gp3 chiffré de 20 Gio et crédits CPU
standard. Une seule AZ pour le worker : démonstration sans haute disponibilité.
Pas de NAT gateway, load balancer, SSH ou ouverture publique de NodePort.
L'accès à l'application sera démontré par port-forward Kubernetes authentifié.
AWS crée aussi les dépendances du service : EC2, ASG, ENI, groupes de sécurité,
volume système et éventuellement service-linked roles. Le nombre de ressources
Terraform ne représente donc pas le nombre total d'objets AWS créés.

MySQL, l'application, Prometheus et Grafana seront installés ensuite dans ce cluster,
avec volumes gp3 dynamiques : hypothèse de 6 Gio supplémentaires. Ils ne figurent
pas dans le premier plan infrastructure. Leurs preuves et captures restent à produire.
LabRole est réutilisé par EBS CSI via IMDSv2 : compromis du laboratoire personnel ;
hors lab, préférer un rôle dédié à chaque service.

## Estimation prudente, pas un plafond automatique

| Poste | Hypothèse horaire USD |
| --- | ---: |
| EKS en support standard | 0,1000 |
| Un t3.medium Linux | environ 0,0416 ; provision de calcul 0,0500 |
| Une IPv4 publique du worker | 0,0050 |
| 26 Gio gp3 au total, hypothèse 0,08 USD/Go-mois | environ 0,0029 |
| Total arrondi pour décider | **0,16 USD/h** |

Environ 0,65 USD pour quatre heures ou 3,85 USD pour 24 heures, hors transfert,
taxes et différences de facturation. Objectif : démonstration courte puis nettoyage ;
ne pas laisser EKS fonctionner plusieurs jours sur le budget de 50 USD.
L'API Pricing du lab est refusée ; ce calcul utilise les prix publics AWS et une
marge sur EC2, sans prétendre mesurer la facture réelle du Learner Lab.

Sources consultées le 29 octobre 2026 : [EKS](https://aws.amazon.com/eks/pricing/),
[EC2 us-east-1](https://docs.aws.amazon.com/prescriptive-guidance/latest/optimize-costs-microsoft-workloads/right-size-selection.html),
[IPv4](https://aws.amazon.com/vpc/pricing/), [exemple gp3](https://aws.amazon.com/ebs/pricing/).

## Interventions nécessaires

AGENTS.md exige une revue et un accord explicite avant Terraform apply. Cet accord
a été obtenu pour le plan identifié ci-dessus, avant son exécution.
kubectl 1.36.6 a été téléchargé après accord et vérifié par SHA-256 dans .local/tools/,
sans modification du PATH. Le client historique 1.32 reste disponible pour kind.

Avant fermeture du lab : exporter les preuves, identifier et supprimer les PVC de
démonstration, vérifier les volumes EBS, préparer et faire approuver le plan destroy.
Ne pas supposer que End Lab supprime tous les objets et arrête tous les frais.
