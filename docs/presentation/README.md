# Présentation à utiliser

- [PowerPoint éditable](eventsProject-Tache-Soutenance.pptx)
- [PDF prêt à partager](eventsProject-Tache-Soutenance.pdf)
- [Aperçu des 26 slides](preview-ready/contact-sheet.jpg)
- [Plan](presentation-plan.md) et [index des preuves](screenshots-index.md)

Version du 31 octobre 2026 : DTO explicites, 52 tests, JaCoCo, Sonar et pipeline
unique actualisés avec les résultats de Jenkins #15. Les captures AWS du 29 octobre
sont datées comme historiques. Le deck ne prétend pas qu'EKS est actif actuellement.
Descriptions visibles uniquement ; aucune note orateur. Les anciens decks sont conservés.

## Régénérer et contrôler

Depuis la racine, avec PowerPoint déjà installé sur Windows :

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File docs/presentation/Build-ReadyPresentation.ps1
python docs/presentation/Validate-ReadyPresentation.py
```

Source éditable : slides-ready.json. Le générateur produit le PPTX, le PDF et les
26 previews sans fermer les présentations déjà ouvertes. Le validateur contrôle
les textes exacts, les notes vides, les labels historiques, le nombre de pages,
les exports et le rapport de débordement PowerPoint.
