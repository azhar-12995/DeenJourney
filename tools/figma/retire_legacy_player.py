"""Remove the replaced prototype player from the shared screens file."""
from pathlib import Path

root = Path(__file__).resolve().parents[2]
path = root / 'composeApp/src/commonMain/kotlin/com/deenjourney/app/feature/quran/QuranScreens.kt'
source = path.read_text(encoding='utf-8')
start = source.find('@Composable\nfun QuranPlayerScreen()')
if start >= 0:
    end = source.index('@Composable\nfun ReciterPickerScreen()', start)
    source = source[:start] + source[end:]
source = source.replace('private fun audioTime(ms: Long): String = "${ms / 60000}:${((ms / 1000) % 60).toString().padStart(2, \'0\')}"\n', '')
path.write_text(source, encoding='utf-8')
