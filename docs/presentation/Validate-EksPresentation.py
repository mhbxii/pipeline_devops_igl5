"""Validate the AWS deck using the common exact-text and empty-notes checks."""
from pathlib import Path

root = Path(__file__).resolve().parents[2]
validator = root / 'scripts/Validate-Presentation.py'
source = validator.read_text(encoding='utf-8')
source = source.replace('"eventsProject-Tache.pptx"', '"eventsProject-Tache-AWS-EKS.pptx"')
source = source.replace('"preview-current"', '"preview-eks"')
exec(compile(source, str(validator), 'exec'), {'__file__': str(validator), '__name__': '__main__'})
