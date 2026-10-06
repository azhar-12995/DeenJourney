package com.deenjourney.app.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.core.num
import com.deenjourney.app.design.icons.glyph
import com.deenjourney.app.design.icons.isBrandIcon
import com.deenjourney.app.design.icons.lineIcon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------- text helpers

@Composable
fun Txt(
    text: String, style: TextStyle, color: Color = Dj.c.text, modifier: Modifier = Modifier, align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE, overflow: TextOverflow = TextOverflow.Ellipsis,
) = Text(text, modifier, color = color, style = style, textAlign = align, maxLines = maxLines, overflow = overflow)

/** Arabic / Quranic / Urdu block: always laid out right-to-left whatever the UI language. */
@Composable
fun ArabicText(text: String, style: TextStyle, color: Color = Dj.c.text, modifier: Modifier = Modifier, center: Boolean = false, maxLines: Int = Int.MAX_VALUE) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text, modifier, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
            style = style.copy(textDirection = TextDirection.Rtl, textAlign = if (center) TextAlign.Center else TextAlign.Start),
        )
    }
}

@Composable
fun Overline(text: String, modifier: Modifier = Modifier, color: Color = Dj.c.text3) =
    Txt(if (LocalLang.current.rtl) text else text.uppercase(), Dj.type.overline, color, modifier)

@Composable
fun Hr(modifier: Modifier = Modifier) = HorizontalDivider(modifier, thickness = 1.dp, color = Dj.c.divider)

// ---------------------------------------------------------------- icons & glyphs

private val MIRROR = setOf("arrow-left", "arrow-right", "chevron-left", "chevron-right", "log-out", "send", "external-link", "skip-back", "skip-forward", "repeat")
private val NO_MIRROR_MEDIA = setOf("skip-back", "skip-forward", "repeat")

@Composable
fun DjIcon(name: String, size: Dp = 20.dp, tint: Color = Dj.c.primary, modifier: Modifier = Modifier, description: String? = null, mirror: Boolean = true) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val flip = mirror && rtl && name in MIRROR && name !in NO_MIRROR_MEDIA
    Image(
        lineIcon(name), description,
        modifier.size(size).graphicsLayer { if (flip) scaleX = -1f },
        colorFilter = if (isBrandIcon(name)) null else ColorFilter.tint(tint),
    )
}

enum class Tone { Gold, Green, White, Cream, Rose, Blue }

@Composable
fun glyphColors(tone: Tone): Pair<Color, Color> {
    val dark = Dj.c.isDark
    return when (tone) {
        Tone.Gold -> if (dark) Color(0xFFE0C893) to Color(0xFF4A3F22) else Color(0xFF8C6D2E) to Color(0xFFEBD9AE)
        Tone.Green -> if (dark) Color(0xFF8BD4AE) to Color(0xFF24443A) else Color(0xFF17533C) to Color(0xFFCFE3D4)
        Tone.White -> Color.White to Color(0xFF5C9C7E)
        Tone.Cream -> Color(0xFFF3E3BC) to Color(0xFF2E8060)
        Tone.Rose -> if (dark) Color(0xFFE79A9A) to Color(0xFF3A2020) else Color(0xFFB04A4A) to Color(0xFFF6D3CF)
        Tone.Blue -> if (dark) Color(0xFF8CC7E6) to Color(0xFF15303D) else Color(0xFF2C6E91) to Color(0xFFCDE4EF)
    }
}

@Composable
fun Glyph(name: String, size: Dp = 28.dp, tone: Tone = Tone.Gold, modifier: Modifier = Modifier) {
    val (s, a) = glyphColors(tone)
    Image(glyph(name, s, a), null, modifier.size(size))
}

@Composable
fun GlyphTile(name: String, size: Dp = 44.dp, tone: Tone = Tone.Gold, modifier: Modifier = Modifier, shape: Shape = CircleShape, bg: Color? = null, glyphSize: Dp = size * 0.62f) {
    val fill = bg ?: when (tone) {
        Tone.Green -> Dj.c.primaryTint
        Tone.Rose -> Dj.c.roseTint
        Tone.Blue -> Dj.c.infoTint
        Tone.White, Tone.Cream -> Color.Transparent
        Tone.Gold -> Dj.c.goldTint
    }
    Box(modifier.size(size).clip(shape).background(fill), contentAlignment = Alignment.Center) { Glyph(name, glyphSize, tone) }
}

@Composable
fun IconTile(icon: String, size: Dp = 40.dp, bg: Color = Dj.c.primaryTint, tint: Color = Dj.c.primary, shape: Shape = RoundedCornerShape(12.dp), iconSize: Dp = size * 0.5f) {
    Box(Modifier.size(size).clip(shape).background(bg), contentAlignment = Alignment.Center) { DjIcon(icon, iconSize, tint) }
}

// ---------------------------------------------------------------- interaction

fun Modifier.tap(enabled: Boolean = true, role: Role = Role.Button, onClick: (() -> Unit)?): Modifier =
    if (onClick == null) this else this.clickable(enabled = enabled, role = role, onClick = onClick)

@Composable
fun IconBtn(icon: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Dj.c.text, size: Dp = 40.dp, iconSize: Dp = 22.dp, bg: Color = Color.Transparent, description: String? = null, badge: Boolean = false) {
    Box(
        modifier.size(size).clip(CircleShape).background(bg)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(bounded = true), role = Role.Button, onClick = onClick)
            .semantics { if (description != null) contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        DjIcon(icon, iconSize, tint)
        if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(9.dp).clip(CircleShape).background(Dj.c.danger).border(2.dp, Dj.c.surface, CircleShape))
    }
}

enum class BtnStyle { Primary, Secondary, Soft, Light, Ghost, Gold, GoldSoft, Danger, DangerSoft, White }

@Composable
fun DjButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: BtnStyle = BtnStyle.Primary,
    lead: String? = null, trail: String? = null, enabled: Boolean = true, loading: Boolean = false, small: Boolean = false,
) {
    val c = Dj.c
    val (fill, border, fg) = when (style) {
        BtnStyle.Primary -> Triple(c.primary, null, c.onPrimary)
        BtnStyle.Secondary -> Triple(c.surface, c.primary, c.primary)
        BtnStyle.Soft -> Triple(c.primaryTint, null, c.primary)
        BtnStyle.Light -> Triple(c.surface, c.border, c.text)
        BtnStyle.Ghost -> Triple(Color.Transparent, null, c.primary)
        BtnStyle.Gold -> Triple(c.gold, null, Color.White)
        BtnStyle.GoldSoft -> Triple(c.goldTint, null, c.goldText)
        BtnStyle.Danger -> Triple(c.danger, null, if (c.isDark) Color.Black else Color.White)
        BtnStyle.DangerSoft -> Triple(c.dangerTint, null, c.danger)
        BtnStyle.White -> Triple(Color.White, null, Color(0xFF17533C))
    }
    val shape = RoundedCornerShape(if (small) 12.dp else 14.dp)
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier
            .then(if (style == BtnStyle.Primary && enabled) Modifier.shadow(6.dp, shape, ambientColor = c.primary, spotColor = c.primary.copy(alpha = 0.4f)) else Modifier)
            .clip(shape).background(fill.copy(alpha = fill.alpha * alpha))
            .then(if (border != null) Modifier.border(1.4.dp, border.copy(alpha = alpha), shape) else Modifier)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .heightIn(min = if (small) 38.dp else 50.dp)
            .padding(horizontal = if (small) 14.dp else 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = fg, strokeWidth = 2.dp)
        else if (lead != null) DjIcon(lead, if (small) 17.dp else 19.dp, fg.copy(alpha = alpha))
        if (text.isNotEmpty()) Txt(text, if (small) Dj.type.labelM else Dj.type.button, fg.copy(alpha = alpha), maxLines = 1)
        if (trail != null) DjIcon(trail, 18.dp, fg.copy(alpha = alpha))
    }
}

@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Dj.c.primary, style: TextStyle = Dj.type.labelM) =
    Txt(text, style, color, modifier.clip(RoundedCornerShape(6.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 4.dp, vertical = 4.dp))

// ---------------------------------------------------------------- chips, tabs, segmented

@Composable
fun DjChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: String? = null, caret: Boolean = false, quiet: Boolean = false) {
    val c = Dj.c
    val fill = if (selected) c.primary else if (quiet) c.surface2 else c.surface
    val fg = if (selected) c.onPrimary else c.text2
    Row(
        modifier.clip(CircleShape).background(fill)
            .then(if (!selected && !quiet) Modifier.border(1.dp, c.border, CircleShape) else Modifier)
            .clickable(role = Role.Tab, onClick = onClick).heightIn(min = 34.dp).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) DjIcon(icon, 14.dp, fg)
        Txt(text, Dj.type.labelM, fg, maxLines = 1)
        if (caret) DjIcon("chevron-down", 14.dp, fg)
    }
}

@Composable
fun ChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) =
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Dj.c
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.surface2).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            val bg by animateColorAsState(if (on) c.primary else Color.Transparent)
            Box(
                Modifier.weight(1f).heightIn(min = 36.dp).clip(RoundedCornerShape(10.dp)).background(bg)
                    .clickable(role = Role.Tab) { onSelect(i) }.padding(horizontal = 6.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) { Txt(o, Dj.type.labelM, if (on) c.onPrimary else c.text2, align = TextAlign.Center, maxLines = 2) }
        }
    }
}

@Composable
fun UTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Dj.c
    Row(modifier.fillMaxWidth()) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            Column(Modifier.weight(1f).clickable(role = Role.Tab) { onSelect(i) }.padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt(o, if (on) Dj.type.titleS else Dj.type.labelM, if (on) c.primary else c.text3, align = TextAlign.Center, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(if (on) 2.5.dp else 1.dp).background(if (on) c.primary else c.divider))
            }
        }
    }
}

// ---------------------------------------------------------------- containers

@Composable
fun DjCard(
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, padding: PaddingValues = PaddingValues(14.dp),
    fill: Color = Dj.c.surface, brush: Brush? = null, border: Color? = Dj.c.border, radius: Dp = 16.dp, elevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier
            .then(if (elevated && !Dj.c.isDark) Modifier.shadow(3.dp, shape, ambientColor = Color(0x22182118), spotColor = Color(0x14182118)) else Modifier)
            .clip(shape)
            .then(if (brush != null) Modifier.background(brush) else Modifier.background(fill))
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .tap(onClick = onClick)
            .padding(padding),
        content = content,
    )
}

@Composable
fun ListRow(
    title: String, modifier: Modifier = Modifier, sub: String? = null, lead: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null, chevron: Boolean = false, onClick: (() -> Unit)? = null,
    titleStyle: TextStyle = Dj.type.titleS, titleColor: Color = Dj.c.text, padding: PaddingValues = PaddingValues(vertical = 11.dp), subMaxLines: Int = 3,
    extra: (@Composable () -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().tap(onClick = onClick).padding(padding), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        lead?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Txt(title, titleStyle, titleColor)
            if (!sub.isNullOrBlank()) Txt(sub, Dj.type.bodyS, Dj.c.text2, maxLines = subMaxLines)
            extra?.invoke()
        }
        trailing?.invoke()
        if (chevron) DjIcon("chevron-right", 18.dp, Dj.c.text3)
    }
}

/** A list row in its own card (the most common block in the design). */
@Composable
fun CardRow(
    title: String, modifier: Modifier = Modifier, sub: String? = null, glyph: String? = null, tone: Tone = Tone.Gold, icon: String? = null,
    lead: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null, chevron: Boolean = true, onClick: (() -> Unit)? = null,
    extra: (@Composable () -> Unit)? = null, fill: Color = Dj.c.surface, border: Color = Dj.c.border,
) {
    DjCard(modifier.fillMaxWidth(), onClick = onClick, padding = PaddingValues(horizontal = 14.dp, vertical = 11.dp), radius = 14.dp, fill = fill, border = border, elevated = false) {
        ListRow(
            title, sub = sub, padding = PaddingValues(0.dp), chevron = chevron, trailing = trailing, extra = extra,
            lead = lead ?: glyph?.let { { GlyphTile(it, 42.dp, tone) } } ?: icon?.let { { IconTile(it) } },
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null, style: TextStyle = Dj.type.titleM) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Txt(title, style, Dj.c.text, Modifier.weight(1f))
        if (action != null && onAction != null) TextLink(action, onAction)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, icon: String? = null, fill: Color = Dj.c.primaryTint, color: Color = Dj.c.primary, onClick: (() -> Unit)? = null, style: TextStyle = Dj.type.labelS) {
    Row(modifier.clip(CircleShape).background(fill).tap(onClick = onClick).padding(horizontal = 10.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) DjIcon(icon, 13.dp, color)
        Txt(text, style, color, maxLines = 1)
    }
}

@Composable
fun NumBadge(n: Int, size: Dp = 30.dp, fill: Color = Dj.c.primary, color: Color = Dj.c.onPrimary) {
    Box(Modifier.size(size).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) { Txt(LocalLang.current.num(n), Dj.type.labelM, color) }
}

enum class NoteTone { Green, Gold, Grey, Red, Blue }

@Composable
fun NoteBox(text: String, modifier: Modifier = Modifier, icon: String = "info", glyph: String? = null, title: String? = null, tone: NoteTone = NoteTone.Green, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val c = Dj.c
    val (bg, fg) = when (tone) {
        NoteTone.Green -> c.primaryTint to c.primary
        NoteTone.Gold -> c.goldTint to c.goldText
        NoteTone.Grey -> c.surface2 to c.text2
        NoteTone.Red -> c.dangerTint to c.danger
        NoteTone.Blue -> c.infoTint to c.info
    }
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).tap(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (glyph != null) Glyph(glyph, 26.dp, if (tone == NoteTone.Green) Tone.Green else Tone.Gold) else DjIcon(icon, 19.dp, fg)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) Txt(title, Dj.type.titleS, c.text)
            Txt(text, Dj.type.bodyS, if (title != null) c.text2 else c.text)
        }
        trailing?.invoke()
    }
}

// ---------------------------------------------------------------- toggles & progress

@Composable
fun DjSwitch(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = Dj.c
    val track by animateColorAsState(if (checked) c.primary else c.border)
    val offset by animateDpAsState(if (checked) 18.dp else 0.dp)
    Box(
        modifier.size(44.dp, 26.dp).clip(CircleShape).background(track.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) }.padding(3.dp),
    ) { Box(Modifier.offset(x = offset).size(20.dp).clip(CircleShape).background(Color.White)) }
}

@Composable
fun DjCheck(checked: Boolean, onChange: ((Boolean) -> Unit)? = null, round: Boolean = false, size: Dp = 22.dp) {
    val c = Dj.c
    val shape = if (round) CircleShape else RoundedCornerShape(6.dp)
    Box(
        Modifier.size(size).clip(shape).background(if (checked) c.primary else c.surface)
            .then(if (!checked) Modifier.border(1.5.dp, c.placeholder, shape) else Modifier)
            .tap(role = Role.Checkbox, onClick = onChange?.let { { it(!checked) } }),
        contentAlignment = Alignment.Center,
    ) { if (checked) DjIcon("check", size * 0.64f, c.onPrimary) }
}

@Composable
fun DjRadio(selected: Boolean) {
    val c = Dj.c
    Box(Modifier.size(22.dp).clip(CircleShape).background(c.surface).border(if (selected) 2.dp else 1.5.dp, if (selected) c.primary else c.placeholder, CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(c.primary))
    }
}

@Composable
fun DjProgress(pct: Float, modifier: Modifier = Modifier, height: Dp = 6.dp, color: Color = Dj.c.primary, track: Color = Dj.c.primaryTint) {
    val p by animateFloatAsState(pct.coerceIn(0f, 1f))
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxWidth(p).fillMaxHeight().clip(CircleShape).background(color))
    }
}

@Composable
fun Ring(pct: Float, size: Dp = 64.dp, stroke: Dp = 6.dp, label: String? = null, color: Color = Dj.c.primary, track: Color = Dj.c.primaryTint, labelStyle: TextStyle = Dj.type.titleS) {
    val p by animateFloatAsState(pct.coerceIn(0f, 1f))
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val inset = sw / 2
            val arc = Size(this.size.width - sw, this.size.height - sw)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(sw))
            if (p > 0f) drawArc(color, -90f, 360f * p, false, Offset(inset, inset), arc, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Txt(label ?: "${LocalLang.current.num((p * 100).toInt())}%", labelStyle, Dj.c.text, align = TextAlign.Center)
    }
}

/** Ayah-end marker: number inside an eight-point star (outline gold). */
@Composable
fun AyahMarker(n: Int, size: Dp = 30.dp, fill: Color = Dj.c.surface, stroke: Color = Dj.c.gold, textColor: Color = Dj.c.goldText) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2
            val ri = r * 0.80f
            val path = Path()
            for (i in 0 until 16) {
                val rad = if (i % 2 == 0) r else ri
                val a = PI / 8 * i - PI / 2
                val x = center.x + (rad * cos(a)).toFloat()
                val y = center.y + (rad * sin(a)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, fill)
            drawPath(path, stroke, style = Stroke(1.2.dp.toPx()))
        }
        Txt(LocalLang.current.num(n), Dj.type.labelS, textColor, align = TextAlign.Center)
    }
}

// ---------------------------------------------------------------- inputs

@Composable
fun DjField(
    value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, label: String? = null, placeholder: String = "",
    icon: String? = null, trailing: (@Composable () -> Unit)? = null, error: String? = null, help: String? = null,
    password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text, ime: ImeAction = ImeAction.Next,
    onIme: (() -> Unit)? = null, singleLine: Boolean = true, minLines: Int = 1, enabled: Boolean = true,
) {
    val c = Dj.c
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) Txt(label, Dj.type.labelM, c.text)
        val borderColor = when { error != null -> c.danger; focused -> c.primary; else -> c.border }
        BasicTextField(
            value, onChange, enabled = enabled, singleLine = singleLine, minLines = minLines,
            textStyle = Dj.type.bodyM.copy(color = c.text),
            cursorBrush = SolidColor(c.primary),
            visualTransformation = if (password && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard, imeAction = ime),
            keyboardActions = KeyboardActions(onAny = { onIme?.invoke() }),
            modifier = Modifier.fillMaxWidth().onFocusChangedCompat { focused = it },
            decorationBox = { inner ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).background(c.surface)
                        .border(if (focused || error != null) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (icon != null) DjIcon(icon, 18.dp, if (focused) c.primary else c.text3)
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Txt(placeholder, Dj.type.bodyM, c.placeholder, maxLines = 1)
                        inner()
                    }
                    if (password) IconBtn(if (reveal) "eye-off" else "eye", { reveal = !reveal }, size = 32.dp, iconSize = 18.dp, tint = c.text3)
                    trailing?.invoke()
                }
            },
        )
        when {
            error != null -> Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) { DjIcon("circle-alert", 14.dp, c.danger); Txt(error, Dj.type.caption, c.danger) }
            help != null -> Txt(help, Dj.type.caption, c.text3)
        }
    }
}

@Composable
fun SearchBox(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null, autofocus: Boolean = false) {
    val c = Dj.c
    BasicTextField(
        value, onChange, singleLine = true, textStyle = Dj.type.bodyM.copy(color = c.text), cursorBrush = SolidColor(c.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { onSubmit?.invoke() }),
        modifier = modifier.fillMaxWidth().autofocusCompat(autofocus),
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(14.dp)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(14.dp)).padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DjIcon("search", 18.dp, c.text3)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Txt(placeholder, Dj.type.bodyM, c.placeholder, maxLines = 1)
                    inner()
                }
                if (value.isNotEmpty()) IconBtn("circle-x", { onChange("") }, size = 34.dp, iconSize = 18.dp, tint = c.text3)
            }
        },
    )
}

/** Fake search box that navigates to a search screen. */
@Composable
fun SearchLink(placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Dj.c
    Row(
        modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(14.dp)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) { DjIcon("search", 18.dp, c.text3); Txt(placeholder, Dj.type.bodyM, c.placeholder, maxLines = 1) }
}

// ---------------------------------------------------------------- states

@Composable
fun EmptyState(art: String, title: String, body: String, modifier: Modifier = Modifier, primary: String? = null, primaryIcon: String? = null, onPrimary: (() -> Unit)? = null, secondary: String? = null, onSecondary: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Art(art, Modifier.size(260.dp, 195.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        Txt(title, Dj.type.headline, Dj.c.text, align = TextAlign.Center)
        Txt(body, Dj.type.bodyM, Dj.c.text2, align = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        if (primary != null && onPrimary != null) DjButton(primary, onPrimary, Modifier.fillMaxWidth(), lead = primaryIcon)
        if (secondary != null && onSecondary != null) DjButton(secondary, onSecondary, Modifier.fillMaxWidth(), style = BtnStyle.Ghost)
    }
}

@Composable
fun Skeleton(modifier: Modifier, radius: Dp = 12.dp) = Box(modifier.clip(RoundedCornerShape(radius)).background(Dj.c.surface2))

@Composable
fun StatBox(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = Dj.c.text, valueStyle: TextStyle = Dj.type.numberM) {
    DjCard(modifier, padding = PaddingValues(12.dp), elevated = false) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Txt(value, valueStyle, valueColor, align = TextAlign.Center)
            Txt(label, Dj.type.caption, Dj.c.text2, align = TextAlign.Center)
        }
    }
}

@Composable
fun BorderBox(modifier: Modifier = Modifier, selected: Boolean, onClick: () -> Unit, radius: Dp = 14.dp, content: @Composable BoxScope.() -> Unit) {
    val c = Dj.c
    val shape = RoundedCornerShape(radius)
    Box(modifier.clip(shape).background(if (selected) c.primaryTint else c.surface).border(if (selected) 1.5.dp else 1.dp, if (selected) c.primary else c.border, shape).clickable(onClick = onClick), content = content)
}

fun Modifier.pressScale(pressed: Boolean): Modifier = this.scale(if (pressed) 0.97f else 1f)

@Composable
fun Gap(h: Dp) = Spacer(Modifier.height(h))

val NoBorder: BorderStroke? = null
