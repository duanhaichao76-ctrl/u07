"""Fetch KanjiVG kana SVGs (CC BY-SA 3.0). Avoid guessing stroke orders."""
from pathlib import Path
import subprocess
import shutil
import tempfile

root = Path(__file__).resolve().parents[1]
output = root / 'app/src/main/assets/strokes'
output.mkdir(parents=True, exist_ok=True)
chars = ''.join(chr(n) for n in range(0x3041, 0x3097)) + ''.join(chr(n) for n in range(0x30A1, 0x30FB))
with tempfile.TemporaryDirectory() as tmp:
    subprocess.run(['git', 'clone', '--depth', '1', 'https://github.com/KanjiVG/kanjivg.git', tmp + '/kanjivg'], check=True)
    base = Path(tmp) / 'kanjivg' / 'kanji'
    count = 0
    for c in chars:
        stem = f'{ord(c):05x}'
        src = base / (stem + '.svg')
        if src.exists():
            shutil.copyfile(src, output / (stem + '.svg'))
            count += 1
    print(f'Bundled {count} KanjiVG character files under CC BY-SA 3.0')
