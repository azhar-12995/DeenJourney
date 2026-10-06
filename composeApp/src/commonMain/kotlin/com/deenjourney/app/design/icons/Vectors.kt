package com.deenjourney.app.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** One shape of a line icon. [fill] = "" (stroke only, tinted) or "xRRGGBB" (fixed colour, e.g. brand marks). */
internal class IP(val d: String, val fill: String)

/** One shape of a duotone glyph. Colours: "S" stroke colour, "A" accent colour, "xRRGGBB" fixed, "" none. */
internal class GP(val d: String, val fill: String, val stroke: String, val sw: Float, val alpha: Float)

private fun hex(v: String): Color = Color(0xFF000000 or v.substring(1).toLong(16))

private val iconCache = HashMap<String, ImageVector>()
private val glyphCache = HashMap<String, ImageVector>()

/** Lucide-style 24×24 line icon (stroke 2). Tint it with Icon(tint = …). Unknown names fall back to a circle. */
fun lineIcon(name: String): ImageVector = iconCache.getOrPut(name) {
    val parts = ICON_DATA[name] ?: ICON_DATA.getValue("circle-dot")
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        for (p in parts) {
            if (p.fill.isNotEmpty()) addPath(addPathNodes(p.d), fill = SolidColor(hex(p.fill)))
            else addPath(
                addPathNodes(p.d), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
}

/** True when the icon is a multi-colour brand mark that must not be tinted. */
fun isBrandIcon(name: String): Boolean = name.startsWith("brand-")

/** 48×48 duotone glyph in the given stroke [s] and accent [a] colours. */
fun glyph(name: String, s: Color, a: Color): ImageVector = glyphCache.getOrPut("$name|${s.value}|${a.value}") {
    val parts = GLYPH_DATA[name] ?: GLYPH_DATA.getValue("star")
    fun col(t: String): Color? = when {
        t == "S" -> s
        t == "A" -> a
        t.startsWith("x") -> hex(t)
        else -> null
    }
    ImageVector.Builder(name, 48.dp, 48.dp, 48f, 48f).apply {
        for (p in parts) {
            val f = col(p.fill)
            val st = col(p.stroke)
            addPath(
                addPathNodes(p.d),
                fill = f?.let { SolidColor(it) }, fillAlpha = p.alpha,
                stroke = st?.let { SolidColor(it) }, strokeAlpha = p.alpha, strokeLineWidth = p.sw,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
}
