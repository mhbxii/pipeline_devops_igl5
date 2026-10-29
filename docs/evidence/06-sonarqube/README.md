# Périmètre des preuves Sonar

quality-gate.json, project-metrics.json et vulnerabilities.json décrivent la
baseline initiale : quatre vulnérabilités et gate aux conditions vides.
Ils sont conservés pour montrer le diagnostic, pas le résultat final.

Les fichiers build-N-* décrivent les tâches et callbacks réellement observés
dans Jenkins. Le build #4 a corrigé les quatre entrées et passé un gate évalué :
81,9 % nouveau code, 49,6 % global, zéro vulnérabilité. Les builds suivants analysent
les mêmes sources applicatives ; consulter le dernier build de livraison réussi.

Les PNG sonar-final-* sont de vraies captures Edge des résultats après correction.
Une couverture globale ne doit pas être présentée comme la couverture du nouveau code.
