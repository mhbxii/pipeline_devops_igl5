# Réactivation EKS — revue du 30 octobre 2026

Budget restant déclaré par l'utilisateur : 49 USD. Session STS renouvelée et compte
conforme au garde-fou Terraform. Aucun cluster existant ; state AWS sans ressource
gérée. Les deux sorties réseau hôte/Jenkins coïncident ; CIDR /32 actualisé dans
les variables privées. Aucun apply effectué au moment de cette revue.

## Plan local préparatoire

Fichier ignoré `.local/aws-eks-reactivation.tfplan`, SHA-256 :
`ce029b8cd74e5bead2244a2c1c9dffedcb139914358359798e649a77311b49b3`.
Terraform détaillé : exit 2, **4 créations, 0 modification, 0 suppression**.

- Cluster events-tache-ayyoub, EKS 1.36, us-east-1, support STANDARD.
- Launch template : disque système gp3 chiffré 20 Gio, IMDSv2, crédits CPU standard.
- Node group : exactement un t3.medium, capacité ON_DEMAND, une AZ.
- Add-on EBS CSI pour les volumes persistants.

VPC, subnets et LabRole existants sont lus. AWS crée les dépendances du service
(EC2, ASG, interfaces, security groups, disque système). Le bootstrap des workloads
créera ensuite 6 Gio de volumes gp3 : MySQL 4, Prometheus 1 et Grafana 1.
Pas de NAT gateway ni load balancer ; accès applicatif par tunnel authentifié.

## Coût estimatif

Provision prudente : environ **0,16 USD/h**, **0,65 USD pour quatre heures**,
hors transfert et taxes. Ce n'est pas un plafond automatique. Estimation basée sur
EKS standard 0,10 USD/h, t3.medium environ 0,0416 USD/h (marge de calcul à 0,05),
IPv4 0,005 USD/h et hypothèse gp3 0,08 USD/Go-mois pour 26 Gio.
L'API Pricing du lab n'est pas disponible ; estimation issue des pages publiques.

Sources consultées le 30 octobre 2026 :
[EKS](https://aws.amazon.com/eks/pricing/),
[t3.medium us-east-1](https://docs.aws.amazon.com/prescriptive-guidance/latest/optimize-costs-microsoft-workloads/right-size-selection.html),
[IPv4](https://aws.amazon.com/vpc/pricing/),
[exemple gp3](https://aws.amazon.com/ebs/pricing/).

## Chemin Jenkins

Jenkins reconstruit et démarré ; outils et montage du state AWS vérifiés.
Build #12 SUCCESS pour CI/gate/package/image locale ; Terraform a été ignoré à
cause de propriétés de paramètres héritées dupliquées. Doublons retirés et script
de configuration corrigé. Build #13 demandé avec RUN_TERRAFORM=true,
APPLY_TERRAFORM=true, DEPLOY_EKS=false et publications désactivées.
Son plan sera examiné avant toute approbation ; le plan local ci-dessus n'autorise
pas à approuver aveuglément un autre plan. L'ancien job de promotion est désactivé.
