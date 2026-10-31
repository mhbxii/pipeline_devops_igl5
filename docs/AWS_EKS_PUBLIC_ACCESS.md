# Accès public AWS à l'API

Autorisation du 31 octobre 2026 : rendre l'application accessible sur AWS sans
arrêter les services existants. Le service supplémentaire `events-public` utilise
les pods applicatifs existants, avec HTTP 80 vers 8089. Le service interne
`events` et les tunnels locaux restent utilisables. Aucun changement de Deployment,
aucune installation, aucun apply Terraform.

Activation volontaire, après autorisation des frais et de l'accès HTTP public :

```powershell
python scripts/Enable-EksPublicAccess.py --enable
```

Le script contrôle compte, cluster et état des quatre workloads avant création.
Il sélectionne le subnet public existant dans l'AZ du worker et utilise le
contrôleur AWS déjà présent pour créer un seul Classic Load Balancer. Le manifeste
est séparé du bootstrap : aucune exposition publique implicite lors d'une future
création du cluster. Jenkins continue à mettre à jour la même application.

La vérification attend réellement HTTP 200 sur `/events/taches` et une readiness
`UP`. Elle compare les UID et compteurs de redémarrage avant/après pour les quatre
workloads. Le résultat et l'URL sont enregistrés dans
`evidence/18-eks-public-access/verification.json` uniquement après réussite.

Jenkins, Nexus, SonarQube, MySQL, Prometheus et Grafana ne sont pas exposés par ce
nouveau service. L'API actuelle n'a pas d'authentification : ses routes sont publiques.
Cet accès de démonstration est HTTP ; aucun certificat ni domaine personnalisé créé.

Coût supplémentaire indicatif en us-east-1 : 0,025 USD/h pour le Classic Load
Balancer, environ 0,005 USD/h pour son IPv4 dans une AZ, puis trafic facturé selon
usage. Sources : [ELB](https://aws.amazon.com/elasticloadbalancing/pricing/) et
[IPv4](https://aws.amazon.com/vpc/pricing/). La facturation continue tant que le
service et son load balancer existent. Aucune suppression effectuée ici.

Pour un futur nettoyage autorisé, supprimer d'abord ce service et attendre la
disparition effective de son load balancer et de ses dépendances avant de supprimer
les workers ou le cluster. La suppression du namespace doit également laisser le
contrôleur terminer le nettoyage ; ne pas forcer les finalizers.
