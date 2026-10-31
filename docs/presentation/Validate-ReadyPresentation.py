"""Validate current French slide content, evidence, empty notes, PDF and layout."""
import json
import re
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image

folder = Path(__file__).resolve().parent
root = folder.parents[1]
entries = json.loads((folder / 'slides-ready.json').read_text(encoding='utf-8-sig'))
preview = folder / 'preview-ready'
ns = {'a': 'http://schemas.openxmlformats.org/drawingml/2006/main'}
with zipfile.ZipFile(folder / 'eventsProject-Tache-Soutenance.pptx') as archive:
    assert archive.testzip() is None, 'Corrupt PPTX archive'
    slides = sorted((p for p in archive.namelist() if re.fullmatch(r'ppt/slides/slide\d+\.xml', p)),
                    key=lambda p: int(re.search(r'slide(\d+)\.xml', p).group(1)))
    assert len(slides) == len(entries)
    for name, entry in zip(slides, entries):
        text = '\n'.join(n.text or '' for n in ET.fromstring(archive.read(name)).findall('.//a:t', ns))
        expected = [entry['title'], entry['description'], entry['evidence']]
        expected += [str(c[key]) for c in entry['cards'] for key in ('title', 'body')]
        for value in expected:
            assert value in text, f'Missing visible text on {name}: {value}'
        for line in entry.get('code', '').splitlines():
            assert not line or line in text, f'Missing code on {name}'
        if entry.get('screenshot'):
            assert (root / 'docs/evidence' / entry['screenshot']).is_file()
        if entry['status'] == 'historical':
            assert 'DÉMONSTRATION DU 29/10/2026' in text, 'Unlabelled historical AWS screenshot'
    for name in archive.namelist():
        if re.fullmatch(r'ppt/notesSlides/notesSlide\d+\.xml', name):
            notes = [n.text for n in ET.fromstring(archive.read(name)).findall('.//a:t', ns)]
            assert not any(n and not n.isdigit() for n in notes), 'Speaker notes must remain empty'
pdf = (folder / 'eventsProject-Tache-Soutenance.pdf').read_bytes()
assert pdf.startswith(b'%PDF-') and b'%%EOF' in pdf[-1024:], 'Invalid PDF export'
pages = len(re.findall(rb'/Type\s*/Page\b', pdf))
assert pages == len(entries), f'PDF page count differs: {pages}'
layout = json.loads((preview / 'layout-check.json').read_text(encoding='utf-8-sig'))
assert layout['slides'] == len(entries)
assert not layout['overflows'], f'Text overflow: {layout["overflows"]}'
images = sorted(preview.glob('Slide*.PNG'), key=lambda p: int(p.stem[5:]))
assert len(images) == len(entries), 'Incomplete slide export'
canvas = Image.new('RGB', (1280, 360 * ((len(images) + 1) // 2)), '#e7edf4')
for index, path in enumerate(images):
    with Image.open(path) as image:
        assert image.size == (1600, 900)
        image.thumbnail((640, 360))
        canvas.paste(image, ((index % 2) * 640, (index // 2) * 360))
canvas.save(preview / 'contact-sheet.jpg', quality=90)
report = {'slides': len(entries), 'pdfPages': pages, 'speakerNotes': 'empty',
          'textOverflow': 0, 'visibleContent': 'exact match',
          'historicalAwsEvidence': 'explicitly dated', 'completePreviews': True}
(preview / 'validation.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
print(json.dumps(report, ensure_ascii=False))
