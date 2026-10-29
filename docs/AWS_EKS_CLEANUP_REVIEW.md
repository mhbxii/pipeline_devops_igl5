# Nettoyage AWS proposé — accord distinct nécessaire

La démonstration est vérifiée et les captures sont conservées. Rien dans cette
préparation ne supprime les ressources. Le cluster reste actif et facturé.

Plan sauvegardé : .local/aws-eks-destroy.tfplan.
SHA-256 : `6f7d712fb3dcad774c4f35280f57759c47b83698ee0ec296269dcd617cb5c8eb`.
Inspection JSON : **0 création, 0 modification, 4 suppressions** :

- aws_eks_addon.ebs : driver EBS CSI ;
- aws_eks_node_group.demo : worker t3.medium et ses dépendances EC2/ASG ;
- aws_eks_cluster.demo : control plane EKS et ses dépendances ;
- aws_launch_template.worker : configuration du worker.

Avant cet apply, supprimer les trois namespaces dédiés events-tache,
events-monitoring et events-grafana, puis attendre la suppression effective des
trois volumes EBS des PVC. Cela efface les données de démonstration MySQL et le
stockage des métriques/dashboards. Le disque système 20 Gio est supprimé avec
l'instance. EBS CSI doit rester actif jusqu'au nettoyage des volumes applicatifs.

Le VPC, les subnets et LabRole restent hors state et ne sont pas supprimés.
Les service-linked roles AWS éventuellement créés automatiquement restent dans
le compte, sans frais d'exécution ; ce plan ne les gère pas.
Le cluster kind, ses volumes et les services Docker locaux restent disponibles.
Les captures, rapports et présentations dans Git sont conservés.

Coût estimé tant que le cluster reste actif : environ 0,16 USD/h plus transfert
et taxes. Ne pas compter sur la fermeture de session pour détruire EKS.
AGENTS.md impose revue et accord avant cet apply destructif.
