"""Check the editable deck against its visible descriptions and export a contact sheet."""
import json
import re
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
folder = root / "docs/presentation"
entries = json.loads((folder / "slides.json").read_text(encoding="utf-8-sig"))
ns = {"a": "http://schemas.openxmlformats.org/drawingml/2006/main"}
with zipfile.ZipFile(folder / "eventsProject-Tache.pptx") as archive:
    assert archive.testzip() is None, "Corrupt PPTX archive"
    slide_files = sorted(
        (n for n in archive.namelist() if re.fullmatch(r"ppt/slides/slide\d+\.xml", n)),
        key=lambda n: int(re.search(r"slide(\d+)\.xml", n).group(1)),
    )
    assert len(slide_files) == len(entries)
    for name, entry in zip(slide_files, entries):
        text = "\n".join(n.text or "" for n in ET.fromstring(archive.read(name)).findall(".//a:t", ns))
        assert entry["title"] in text, name
        assert entry["description"] in text, name
    for name in archive.namelist():
        if re.fullmatch(r"ppt/notesSlides/notesSlide\d+\.xml", name):
            notes = [n.text for n in ET.fromstring(archive.read(name)).findall(".//a:t", ns)]
            assert not any(n and not n.isdigit() for n in notes), "Unexpected speaker notes"
previews = folder / "preview-current"
files = sorted(previews.glob("Slide*.PNG"), key=lambda p: int(p.stem[5:]))
assert len(files) == len(entries), "Incomplete PowerPoint slide export"
canvas = Image.new("RGB", (960, 270 * ((len(files) + 1) // 2)), "#eeeeee")
for i, path in enumerate(files):
    with Image.open(path) as img:
        img.thumbnail((480, 270))
        canvas.paste(img, ((i % 2) * 480, (i // 2) * 270))
canvas.save(previews / "contact-sheet.jpg")
print(f"Verified {len(entries)} slides, exact visible French descriptions, no speaker notes, complete image export.")
