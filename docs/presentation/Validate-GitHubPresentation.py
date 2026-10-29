"""Apply the shared validator to the new deck without touching the open copy."""
from pathlib import Path

root = Path(__file__).resolve().parents[2]
validator = root / "scripts/Validate-Presentation.py"
source = validator.read_text(encoding="utf-8")
source = source.replace('"eventsProject-Tache.pptx"', '"eventsProject-Tache-GitHub.pptx"')
source = source.replace('"preview-current"', '"preview-github"')
exec(compile(source, str(validator), "exec"), {"__file__": str(validator), "__name__": "__main__"})
