# AWS Academy Learner Lab — préparation utilisateur

L'utilisateur confirme que le déploiement AWS/EKS est obligatoire. La livraison
locale kind, Terraform Prometheus et le monitoring sont vérifiés ; ils ne prouvent
pas encore un déploiement EKS. Aucune ressource AWS n'a été créée pour ce projet.

## Ce qui est déjà disponible
AWS CLI 2.31.13 est installé sur Windows. Terraform et kubectl sont déjà disponibles.
Un profil AWS default existe, région eu-north-1 ; il est conservé sans modification.
Sa présence ne prouve ni une session Learner Lab valide ni les permissions EKS.

## À faire dans AWS Academy
1. Ouvrir son propre Learner Lab, cliquer Start Lab et attendre le statut prêt.
2. Relever le budget restant, la durée de session et la région autorisée par le cours.
3. Dans AWS Details, afficher le bloc de credentials AWS CLI de la session.
4. Configurer ces credentials localement dans le profil dédié ci-dessous.

Ne pas coller de credentials dans le chat, les captures ou le dépôt Git. Les trois
valeurs sont nécessaires, notamment aws_session_token. Elles devront être renouvelées
avec les nouvelles sessions. Ne pas remplacer les credentials du profil default.

## Profil local séparé
Ouvrir le fichier de credentials depuis PowerShell :

```powershell
notepad "$env:USERPROFILE\.aws\credentials"
```

Ajouter une section distincte en utilisant les valeurs réelles du lab, localement :

```ini
[events-tache-lab]
aws_access_key_id = VALEUR_DU_LAB
aws_secret_access_key = VALEUR_DU_LAB
aws_session_token = VALEUR_DU_LAB
```

Si ce profil a déjà été ajouté, renouveler uniquement ses trois valeurs. Conserver
les autres profils. Ne pas copier les marqueurs VALEUR_DU_LAB tels quels.

Configurer ensuite la région autorisée. Cet exemple suppose que le cours autorise
us-east-1 ; adapter si la consigne du Learner Lab indique une autre région :

```powershell
aws configure set region us-east-1 --profile events-tache-lab
aws sts get-caller-identity --profile events-tache-lab
```

Le second appel est une vérification d'identité en lecture seule. Il doit réussir.
Pour répondre dans le chat, indiquer simplement que le profil est prêt, la région,
le budget restant, la durée disponible et la limite de budget souhaitée pour la démo.
Les clés, le token et l'identité complète ne sont pas nécessaires dans le chat.

## Ce que l'agent fera après préparation
- Vérifier l'identité du profil dédié et inventorier les ressources en lecture seule.
- Vérifier les permissions EKS et les rôles autorisés par le lab, notamment LabRole
  s'il existe, les subnets et les versions EKS actuellement disponibles.
- Préparer un Terraform AWS séparé du state Prometheus local, avec nom de cluster
  dédié au projet et estimation des coûts adaptée à la région et au budget.
- Prévoir l'accès kubectl, la persistance MySQL et l'adaptation des manifests à EKS.
- Préparer les outils AWS/Terraform dans Jenkins et le renouvellement des credentials
  temporaires, sans exposer les secrets.
- Présenter le plan avant demande d'approbation de l'apply, conformément à AGENTS.md.
- Vérifier ensuite l'API Tache sur EKS, le monitoring et les preuves de présentation.
- Préparer un plan de destruction séparé à revoir et approuver avant suppression.

Le démarrage du lab n'autorise pas un terraform apply. L'approbation doit porter
sur un plan concret et son coût. Aucune utilisation des ressources d'un autre membre
sans autorisation ; pas de reprise aveugle des noms de cluster du document de référence.

## Coûts et périmètre
EKS en support standard est annoncé à 0,10 USD par heure pour le control plane,
soit 2,40 USD sur 24 heures, auxquels s'ajoutent nœuds, disques, IPv4 et autres
ressources éventuelles. Les tarifs effectifs et l'imputation au budget du lab sont
à confirmer avant provisioning. Les crédits du lab constituent un budget à suivre.

Sources consultées le 29 octobre 2026 :
- [Tarification EKS](https://aws.amazon.com/eks/pricing/)
- [Rôle IAM du cluster](https://docs.aws.amazon.com/eks/latest/userguide/cluster-iam-role.html)
- [Rôle IAM des nœuds](https://docs.aws.amazon.com/eks/latest/userguide/create-node-role.html)

Les limites et rôles disponibles varient suivant la configuration du Learner Lab ;
la configuration du cours et l'inventaire du compte priment sur un exemple générique.
