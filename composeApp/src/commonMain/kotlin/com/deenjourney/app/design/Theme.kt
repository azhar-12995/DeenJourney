package com.deenjourney.app.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.res.Res
import com.deenjourney.app.res.amiri_bold
import com.deenjourney.app.res.amiri_quran
import com.deenjourney.app.res.amiri_regular
import com.deenjourney.app.res.inter_bold
import com.deenjourney.app.res.inter_medium
import com.deenjourney.app.res.inter_regular
import com.deenjourney.app.res.inter_semibold
import com.deenjourney.app.res.nastaliq_bold
import com.deenjourney.app.res.nastaliq_regular
import com.deenjourney.app.res.notoarabic_bold
import com.deenjourney.app.res.notoarabic_medium
import com.deenjourney.app.res.notoarabic_regular
import com.deenjourney.app.res.notoarabic_semibold
import com.deenjourney.app.res.playfair_bold
import com.deenjourney.app.res.playfair_semibold
import org.jetbrains.compose.resources.Font

/** Colour tokens — same names and values as the Figma variables (“Color” / “Color Dark”). */
@Immutable
data class DjColors(
    val bg: Color, val bg2: Color, val surface: Color, val surface2: Color, val tile: Color, val border: Color, val divider: Color,
    val text: Color, val text2: Color, val text3: Color, val placeholder: Color,
    val primary: Color, val primaryDark: Color, val primaryBright: Color, val primaryTint: Color, val primarySoft: Color, val onPrimary: Color, val night: Color,
    val gold: Color, val goldText: Color, val goldTint: Color, val goldSoft: Color,
    val danger: Color, val dangerTint: Color, val warning: Color, val warningTint: Color, val info: Color, val infoTint: Color,
    val rose: Color, val roseTint: Color,
    val isDark: Boolean,
) {
    val goldGradient: Brush get() = Brush.horizontalGradient(if (isDark) listOf(Color(0xFF2E2817), Color(0xFF26301F)) else listOf(Color(0xFFF8EED6), Color(0xFFF1E2BF)))
    val mintGradient: Brush get() = Brush.horizontalGradient(if (isDark) listOf(Color(0xFF1C3329), Color(0xFF173027)) else listOf(Color(0xFFE9F2E8), Color(0xFFDCEBDD)))
    val creamGradient: Brush get() = Brush.verticalGradient(if (isDark) listOf(Color(0xFF1E2A24), Color(0xFF16201B)) else listOf(Color(0xFFFBF5E6), Color(0xFFF4EAD2)))
    val greenGradient: Brush get() = Brush.linearGradient(listOf(Color(0xFF1F6A4C), Color(0xFF0B2E22)))
    val nightGradient: Brush get() = Brush.linearGradient(listOf(Color(0xFF0B2E22), Color(0xFF17533C)))
}

val LightColors = DjColors(
    bg = Color(0xFFFAF7EF), bg2 = Color(0xFFF4EFE3), surface = Color(0xFFFFFDF8), surface2 = Color(0xFFF3EEE1), tile = Color(0xFFF6EDD8),
    border = Color(0xFFE7E0CE), divider = Color(0xFFEEE8DA), text = Color(0xFF18211C), text2 = Color(0xFF5A625B), text3 = Color(0xFF737A72),
    placeholder = Color(0xFFA4A99F), primary = Color(0xFF17533C), primaryDark = Color(0xFF0B2E22), primaryBright = Color(0xFF2E8060),
    primaryTint = Color(0xFFE6EFE6), primarySoft = Color(0xFFCFE3D4), onPrimary = Color.White, night = Color(0xFF0B2E22),
    gold = Color(0xFFB8954F), goldText = Color(0xFF7E6127), goldTint = Color(0xFFF6EDD8), goldSoft = Color(0xFFEBD9AE),
    danger = Color(0xFFB3261E), dangerTint = Color(0xFFFBE9E7), warning = Color(0xFF9A6210), warningTint = Color(0xFFFBF0D9),
    info = Color(0xFF2C6E91), infoTint = Color(0xFFE3F0F6), rose = Color(0xFFC25E5E), roseTint = Color(0xFFFBE8E5), isDark = false,
)

val DarkColors = DjColors(
    bg = Color(0xFF0E1512), bg2 = Color(0xFF121B17), surface = Color(0xFF16201B), surface2 = Color(0xFF1E2A24), tile = Color(0xFF2A2618),
    border = Color(0xFF2C3832), divider = Color(0xFF24302A), text = Color(0xFFEEF1EC), text2 = Color(0xFFB5BDB6), text3 = Color(0xFF8C958E),
    placeholder = Color(0xFF6E776F), primary = Color(0xFF6CC196), primaryDark = Color(0xFF0B2E22), primaryBright = Color(0xFF8BD4AE),
    primaryTint = Color(0xFF1C3329), primarySoft = Color(0xFF24443A), onPrimary = Color(0xFF06140E), night = Color(0xFF06140E),
    gold = Color(0xFFD4B474), goldText = Color(0xFFE0C893), goldTint = Color(0xFF2E2817), goldSoft = Color(0xFF4A3F22),
    danger = Color(0xFFF2B8B5), dangerTint = Color(0xFF3B1C1A), warning = Color(0xFFF0C060), warningTint = Color(0xFF3A2C12),
    info = Color(0xFF8CC7E6), infoTint = Color(0xFF15303D), rose = Color(0xFFE79A9A), roseTint = Color(0xFF3A2020), isDark = true,
)

/** Type ramp (matches the Figma text styles). UI styles switch family with the app language. */
@Immutable
data class DjType(
    val displayL: TextStyle, val displayM: TextStyle, val headline: TextStyle, val titleL: TextStyle, val titleM: TextStyle, val titleS: TextStyle,
    val bodyL: TextStyle, val bodyM: TextStyle, val bodyS: TextStyle, val button: TextStyle, val labelM: TextStyle, val labelS: TextStyle,
    val caption: TextStyle, val overline: TextStyle, val numberXL: TextStyle, val numberL: TextStyle, val numberM: TextStyle,
    val quranL: TextStyle, val quranM: TextStyle, val quranS: TextStyle, val arabicL: TextStyle, val arabicM: TextStyle, val arabicS: TextStyle,
    val urduM: TextStyle, val urduS: TextStyle,
)

@Immutable
data class DjFonts(val playfair: FontFamily, val inter: FontFamily, val amiri: FontFamily, val amiriQuran: FontFamily, val nastaliq: FontFamily, val notoArabic: FontFamily)

val LocalDjColors = staticCompositionLocalOf { LightColors }
val LocalDjType = staticCompositionLocalOf<DjType> { error("DjTheme not set") }
val LocalDjFonts = staticCompositionLocalOf<DjFonts> { error("DjTheme not set") }
/** User text-size preference (1.0 normal, 1.15 large, 1.3 extra large). */
val LocalTextScale = staticCompositionLocalOf { 1f }

object Dj {
    val c: DjColors @Composable get() = LocalDjColors.current
    val type: DjType @Composable get() = LocalDjType.current
    val fonts: DjFonts @Composable get() = LocalDjFonts.current
}

@Composable
fun rememberFonts(): DjFonts {
    val playfair = FontFamily(Font(Res.font.playfair_semibold, FontWeight.SemiBold), Font(Res.font.playfair_bold, FontWeight.Bold))
    val inter = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal), Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold), Font(Res.font.inter_bold, FontWeight.Bold)
    )
    val amiri = FontFamily(Font(Res.font.amiri_regular, FontWeight.Normal), Font(Res.font.amiri_bold, FontWeight.Bold))
    val amiriQuran = FontFamily(Font(Res.font.amiri_quran, FontWeight.Normal))
    val nastaliq = FontFamily(
        Font(Res.font.nastaliq_regular, FontWeight.Normal), Font(Res.font.nastaliq_regular, FontWeight.Medium),
        Font(Res.font.nastaliq_bold, FontWeight.SemiBold), Font(Res.font.nastaliq_bold, FontWeight.Bold)
    )
    val notoArabic = FontFamily(
        Font(Res.font.notoarabic_regular, FontWeight.Normal), Font(Res.font.notoarabic_medium, FontWeight.Medium),
        Font(Res.font.notoarabic_semibold, FontWeight.SemiBold), Font(Res.font.notoarabic_bold, FontWeight.Bold)
    )
    return remember(playfair, inter) { DjFonts(playfair, inter, amiri, amiriQuran, nastaliq, notoArabic) }
}

private val Trim = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

fun buildType(f: DjFonts, lang: Lang, scale: Float): DjType {
    fun s(v: Float): TextUnit = (v * scale).sp
    val ui: FontFamily
    val display: FontFamily
    // Nastaliq needs ~2x line height and slightly smaller size; Arabic UI needs ~1.5x
    val k: Float
    val lh: Float
    when (lang) {
        Lang.EN -> { ui = f.inter; display = f.playfair; k = 1f; lh = 1f }
        Lang.UR -> { ui = f.nastaliq; display = f.nastaliq; k = 0.9f; lh = 1.55f }
        Lang.AR -> { ui = f.notoArabic; display = f.notoArabic; k = 1f; lh = 1.12f }
    }
    fun st(fam: FontFamily, w: FontWeight, size: Float, line: Float, ls: Float = 0f) =
        TextStyle(fontFamily = fam, fontWeight = w, fontSize = s(size * k), lineHeight = s(line * k * lh), letterSpacing = ls.em, lineHeightStyle = Trim)
    val dispW = if (lang == Lang.EN) FontWeight.Bold else FontWeight.Bold
    return DjType(
        displayL = st(display, dispW, 32f, 40f, if (lang == Lang.EN) -0.005f else 0f),
        displayM = st(display, dispW, 26f, 33f),
        headline = st(display, FontWeight.SemiBold, 22f, 29f),
        titleL = st(display, FontWeight.SemiBold, 19f, 25f),
        titleM = st(ui, FontWeight.SemiBold, 16f, 22f),
        titleS = st(ui, FontWeight.SemiBold, 14f, 19f),
        bodyL = st(ui, FontWeight.Normal, 15f, 23f),
        bodyM = st(ui, FontWeight.Normal, 14f, 20f),
        bodyS = st(ui, FontWeight.Normal, 12.5f, 17f),
        button = st(ui, FontWeight.SemiBold, 15f, 20f),
        labelM = st(ui, FontWeight.Medium, 13f, 18f),
        labelS = st(ui, FontWeight.Medium, 11.5f, 15f),
        caption = st(ui, FontWeight.Normal, 11f, 14f),
        overline = st(ui, FontWeight.SemiBold, 11f, 14f, if (lang == Lang.EN) 0.08f else 0f),
        // numbers always use Inter (tabular look) except Arabic-Indic digits which need an Arabic font
        numberXL = st(if (lang == Lang.AR) f.notoArabic else f.inter, FontWeight.Bold, 34f, 40f).copy(lineHeight = s(40f)),
        numberL = st(if (lang == Lang.AR) f.notoArabic else f.inter, FontWeight.Bold, 24f, 30f).copy(lineHeight = s(30f)),
        numberM = st(if (lang == Lang.AR) f.notoArabic else f.inter, FontWeight.Bold, 18f, 24f).copy(lineHeight = s(24f)),
        quranL = TextStyle(fontFamily = f.amiriQuran, fontSize = s(28f), lineHeight = s(58f), lineHeightStyle = Trim),
        quranM = TextStyle(fontFamily = f.amiriQuran, fontSize = s(22f), lineHeight = s(46f), lineHeightStyle = Trim),
        quranS = TextStyle(fontFamily = f.amiriQuran, fontSize = s(17f), lineHeight = s(36f), lineHeightStyle = Trim),
        arabicL = TextStyle(fontFamily = f.amiri, fontWeight = FontWeight.Bold, fontSize = s(26f), lineHeight = s(46f), lineHeightStyle = Trim),
        arabicM = TextStyle(fontFamily = f.amiri, fontSize = s(20f), lineHeight = s(36f), lineHeightStyle = Trim),
        arabicS = TextStyle(fontFamily = f.amiri, fontSize = s(16f), lineHeight = s(28f), lineHeightStyle = Trim),
        urduM = TextStyle(fontFamily = f.nastaliq, fontSize = s(15f), lineHeight = s(32f), lineHeightStyle = Trim),
        urduS = TextStyle(fontFamily = f.nastaliq, fontSize = s(12.5f), lineHeight = s(27f), lineHeightStyle = Trim),
    )
}

@Composable
fun DjTheme(dark: Boolean, textScale: Float = 1f, content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    val fonts = rememberFonts()
    val lang = LocalLang.current
    val type = remember(fonts, lang, textScale) { buildType(fonts, lang, textScale) }
    val scheme = if (dark) darkColorScheme(
        primary = colors.primary, onPrimary = colors.onPrimary, background = colors.bg, onBackground = colors.text,
        surface = colors.surface, onSurface = colors.text, surfaceVariant = colors.surface2, onSurfaceVariant = colors.text2,
        outline = colors.border, error = colors.danger, secondary = colors.gold, tertiary = colors.primaryBright,
    ) else lightColorScheme(
        primary = colors.primary, onPrimary = colors.onPrimary, background = colors.bg, onBackground = colors.text,
        surface = colors.surface, onSurface = colors.text, surfaceVariant = colors.surface2, onSurfaceVariant = colors.text2,
        outline = colors.border, error = colors.danger, secondary = colors.gold, tertiary = colors.primaryBright,
    )
    CompositionLocalProvider(LocalDjColors provides colors, LocalDjType provides type, LocalDjFonts provides fonts, LocalTextScale provides textScale) {
        MaterialTheme(colorScheme = scheme, typography = MaterialTheme.typography.copy(bodyLarge = type.bodyL, bodyMedium = type.bodyM, labelLarge = type.button)) {
            content()
        }
    }
}
