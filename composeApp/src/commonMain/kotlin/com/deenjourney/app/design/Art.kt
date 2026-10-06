package com.deenjourney.app.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deenjourney.app.res.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Illustration registry (art exported by tools/art/export_app.py — same drawings as Figma › Foundations › Illustrations). */
fun artRes(name: String): DrawableResource = when (name) {
    "avatar_boy" -> Res.drawable.art_avatar_boy
    "avatar_girl" -> Res.drawable.art_avatar_girl
    "avatar_man" -> Res.drawable.art_avatar_man
    "avatar_woman" -> Res.drawable.art_avatar_woman
    "avatar_grandpa" -> Res.drawable.art_avatar_grandpa
    "avatar_grandma" -> Res.drawable.art_avatar_grandma
    "compass_dial" -> Res.drawable.art_compass_dial
    "empty_bookmarks" -> Res.drawable.art_empty_bookmarks
    "empty_downloads" -> Res.drawable.art_empty_downloads
    "empty_error" -> Res.drawable.art_empty_error
    "empty_location" -> Res.drawable.art_empty_location
    "empty_offline" -> Res.drawable.art_empty_offline
    "empty_search" -> Res.drawable.art_empty_search
    "hero_hills" -> Res.drawable.art_hero_hills
    "hero_kaaba" -> Res.drawable.art_hero_kaaba
    "hero_lesson" -> Res.drawable.art_hero_lesson
    "hero_madinah" -> Res.drawable.art_hero_madinah
    "hero_prayer_dawn" -> Res.drawable.art_hero_prayer_dawn
    "hero_prayer_day" -> Res.drawable.art_hero_prayer_day
    "hero_prayer_dusk" -> Res.drawable.art_hero_prayer_dusk
    "hero_prayer_night" -> Res.drawable.art_hero_prayer_night
    "hero_ramadan" -> Res.drawable.art_hero_ramadan
    "hero_roadmap" -> Res.drawable.art_hero_roadmap
    "hero_sprout" -> Res.drawable.art_hero_sprout
    "hero_welcome" -> Res.drawable.art_hero_welcome
    "logo_mark" -> Res.drawable.art_logo_mark
    "pose_dua" -> Res.drawable.art_pose_dua
    "pose_jalsa" -> Res.drawable.art_pose_jalsa
    "pose_qawmah" -> Res.drawable.art_pose_qawmah
    "pose_qiyam" -> Res.drawable.art_pose_qiyam
    "pose_ruku" -> Res.drawable.art_pose_ruku
    "pose_salam" -> Res.drawable.art_pose_salam
    "pose_sujood" -> Res.drawable.art_pose_sujood
    "pose_takbir" -> Res.drawable.art_pose_takbir
    "pose_tashahhud" -> Res.drawable.art_pose_tashahhud
    "prophet_adam" -> Res.drawable.art_prophet_adam
    "prophet_ibrahim" -> Res.drawable.art_prophet_ibrahim
    "prophet_muhammad" -> Res.drawable.art_prophet_muhammad
    "prophet_musa" -> Res.drawable.art_prophet_musa
    "prophet_nuh" -> Res.drawable.art_prophet_nuh
    "prophet_yunus" -> Res.drawable.art_prophet_yunus
    "prophet_yusuf" -> Res.drawable.art_prophet_yusuf
    "quran_rehal" -> Res.drawable.art_quran_rehal
    "skyline_dawn" -> Res.drawable.art_skyline_dawn
    "skyline_day" -> Res.drawable.art_skyline_day
    "skyline_dusk" -> Res.drawable.art_skyline_dusk
    "skyline_night" -> Res.drawable.art_skyline_night
    "tasbih_ring" -> Res.drawable.art_tasbih_ring
    else -> Res.drawable.art_prophet_generic
}

@Composable
fun Art(
    name: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    description: String? = null,
) {
    Image(painterResource(artRes(name)), contentDescription = description, modifier = modifier, contentScale = contentScale, alignment = alignment)
}

/** Profile avatar kinds: boy, girl, man, woman, grandpa, grandma. */
@Composable
fun Avatar(kind: String, size: Dp = 48.dp, modifier: Modifier = Modifier, ring: Color? = null, ringWidth: Dp = 2.dp) {
    val k = kind.removePrefix("avatar_").ifBlank { "man" }
    Art(
        "avatar_$k",
        modifier.size(size).clip(CircleShape).border(if (ring != null) ringWidth else 2.dp, ring ?: Dj.c.surface, CircleShape),
        contentScale = ContentScale.Crop,
    )
}
