from pathlib import Path
import json
import re

root = Path(__file__).resolve().parents[2]
features = root / 'composeApp/src/commonMain/kotlin/com/deenjourney/app/feature'
for path in features.rglob('*Screens.kt'):
    text = path.read_text(encoding='utf-8')
    for name in ('madinah', 'sprout', 'roadmap', 'lesson', 'ramadan', 'kaaba', 'hills'):
        text = text.replace(f'Art("{name}"', f'Art("hero_{name}"')
    if path.name == 'QuranScreens.kt':
        text = text.replace('1 -> { if (notes.isEmpty()) NoItems(); notes.forEach', '1 -> { val quranNotes = notes.filter { Regex("[0-9]+:[0-9]+").matches(it.ayahKey) }; if (quranNotes.isEmpty()) NoItems(); quranNotes.forEach')
    if path.name == 'WorshipScreens.kt':
        text = text.replace('count = logs.firstOrNull { it.kind == "dhikr:$kind:${d.id}" }?.value?.coerceIn(0, d.count) ?: 0', 'count = maxOf(count, logs.firstOrNull { it.kind == "dhikr:$kind:${d.id}" }?.value?.coerceIn(0, d.count) ?: 0)')
    path.write_text(text, encoding='utf-8')

packs = root / 'composeApp/src/commonMain/composeResources/files/content'
for path in packs.glob('*.json'):
    value = json.loads(path.read_text(encoding='utf-8'))
    assert value, f'Empty pack: {path.name}'
    assert path.read_bytes() == (root / 'tools/content' / path.name).read_bytes(), f'Different authored/bundled versions: {path.name}'
routes = (root / 'composeApp/src/commonMain/kotlin/com/deenjourney/app/nav/Pending.kt').read_text()
registered = re.findall(r'screen<([^>]+)>', routes)
assert len(registered) == len(set(registered)), 'Duplicate feature routes'
assert 'PendingScreen' not in routes
print(f'Validated {len(list(packs.glob("*.json")))} nonempty JSON packs and {len(registered)} feature routes.')
