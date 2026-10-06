package com.deenjourney.app.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.quran.*
import com.deenjourney.app.data.user.nowMs
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.Loaded
import com.deenjourney.app.feature.NoItems
import com.deenjourney.app.feature.SwitchRow
import com.deenjourney.app.nav.LocalNavigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/** Share payload is identical to the preview, with references on every variant. */
fun ayahShareText(a: Ayah, english: Boolean, urdu: Boolean): String = listOf(
    a.ar,
    if (english) "${a.en}\nSaheeh International" else "",
    if (urdu) "${a.ur}\nFateh Muhammad Jalandhry" else "",
    "${QuranNames.tr[a.sura - 1]} ${a.key} · Deen Journey",
).filter { it.isNotBlank() }.joinToString("\n\n")

@Composable
fun ShareAyahScreen(sura: Int, aya: Int) {
    val repo = koinInject<QuranRepo>(); val nav = LocalNavigator.current
    var theme by remember { mutableStateOf(0) }
    var english by remember { mutableStateOf(true) }; var urdu by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }; var error by remember { mutableStateOf(false) }
    val layer = rememberGraphicsLayer(); val scope = rememberCoroutineScope()
    Loaded("$sura:$aya", { repo.ayah(sura, aya) }) { a ->
        Screen(top = { AppBar(t("Share ayah", "آیت شیئر کریں", "مشاركة الآية"), onBack = { nav.back() }) }, bottom = {
            if (a != null) Footer {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DjButton(t("Copy text", "متن کاپی کریں", "نسخ النص"), { Platform.copyToClipboard(ayahShareText(a, english, urdu)) }, Modifier.weight(1f), style = BtnStyle.Secondary, lead = "copy")
                    DjButton(if (theme == 3) t("Share text", "متن شیئر کریں", "مشاركة النص") else t("Share image", "تصویر شیئر کریں", "مشاركة الصورة"), {
                        if (theme == 3) Platform.shareText(ayahShareText(a, english, urdu)) else scope.launch {
                            busy = true; error = false
                            try {
                                // Wait for changed theme/translation controls to reach the captured layer.
                                withFrameNanos { }; withFrameNanos { }
                                val bitmap = layer.toImageBitmap()
                                val path = "${Platform.cacheDir()}/share/ayah-${a.sura}-${a.aya}-${nowMs()}.png"
                                withContext(Dispatchers.Default) { Platform.writeFile(path, Platform.pngBytes(bitmap)) }
                                Platform.shareFile(path, "image/png")
                            } catch (e: CancellationException) { throw e } catch (_: Exception) { error = true }
                            finally { busy = false }
                        }
                    }, Modifier.weight(1f), lead = "share-2", enabled = !busy)
                }
            }
        }) {
            Body {
                if (a == null) NoItems() else {
                    val bg = when (theme) { 1 -> LightColors.primaryDark; 2 -> LightColors.goldTint; else -> Color(0xFFFBF6EA) }
                    val fg = if (theme == 1) Color.White else LightColors.text
                    val secondary = if (theme == 1) Color(0xFFE4EFE6) else LightColors.text2
                    val gold = if (theme == 1) Color(0xFFE8D5A6) else LightColors.goldText
                    Column(Modifier.fillMaxWidth().drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }; drawLayer(layer)
                    }.clip(RoundedCornerShape(22.dp)).background(bg).border(1.dp, LightColors.goldSoft, RoundedCornerShape(22.dp))
                        .heightIn(min = 380.dp).padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Glyph("names", 32.dp)
                        ArabicText(a.ar, Dj.type.quranM, fg, Modifier.fillMaxWidth(), center = true)
                        if (english) Txt(a.en, Dj.type.bodyM, secondary, Modifier.fillMaxWidth(), align = TextAlign.Center)
                        if (urdu) ArabicText(a.ur, Dj.type.urduM, secondary, Modifier.fillMaxWidth(), center = true)
                        val translators = listOfNotNull(if (english) "Saheeh International" else null, if (urdu) "Fateh Muhammad Jalandhry" else null)
                        Txt("${QuranNames.tr[a.sura - 1]} ${a.key}" + if (translators.isEmpty()) "" else " · " + translators.joinToString(" · "), Dj.type.labelS, gold, align = TextAlign.Center)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Art("logo_mark", Modifier.size(18.dp)); Txt("Deen Journey", Dj.type.caption, gold)
                        }
                    }
                    ChipRow {
                        listOf(t("Cream", "کریم", "كريمي"), t("Night green", "گہرا سبز", "أخضر ليلي"), t("Gold", "سنہری", "ذهبي"), t("Text only", "صرف متن", "نص فقط")).forEachIndexed { i, label -> DjChip(label, theme == i, { theme = i }) }
                    }
                    DjCard(Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                        SwitchRow(t("Include translation", "ترجمہ شامل کریں", "إضافة الترجمة"), english, { english = it })
                        SwitchRow(t("Include Urdu", "اردو شامل کریں", "إضافة الأردية"), urdu, { urdu = it })
                    }
                    if (error) NoteBox(t("Unable to export. Please try again.", "ایکسپورٹ نہیں ہو سکا۔ دوبارہ کوشش کریں۔", "تعذر التصدير. حاول مجددًا."), tone = NoteTone.Red)
                }
            }
        }
    }
}
