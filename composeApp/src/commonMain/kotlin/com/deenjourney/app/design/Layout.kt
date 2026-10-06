package com.deenjourney.app.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.t

fun Modifier.onFocusChangedCompat(block: (Boolean) -> Unit): Modifier = this.onFocusChanged { block(it.isFocused) }

fun Modifier.autofocusCompat(enabled: Boolean): Modifier = if (!enabled) this else composed {
    val r = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { r.requestFocus() } }
    this.focusRequester(r)
}

/** Standard screen: background, status-bar + navigation-bar insets, optional top/bottom bars and snackbar. */
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    top: (@Composable () -> Unit)? = null,
    bottom: (@Composable () -> Unit)? = null,
    background: Color = Dj.c.bg,
    snackbar: SnackbarHostState? = null,
    statusBarPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(background)) {
        Column(Modifier.fillMaxSize()) {
            if (statusBarPadding) Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            top?.invoke()
            Column(Modifier.weight(1f).fillMaxWidth(), content = content)
            if (bottom != null) bottom() else Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
        if (snackbar != null) SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 80.dp))
    }
}

/** Scrollable padded body column (the design's 18 dp gutter, 14 dp spacing). */
@Composable
fun Body(modifier: Modifier = Modifier, gap: Dp = 14.dp, padding: PaddingValues = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 24.dp), scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(padding),
        verticalArrangement = Arrangement.spacedBy(gap), content = content,
    )
}

@Composable
fun AppBar(
    title: String, modifier: Modifier = Modifier, sub: String? = null, onBack: (() -> Unit)? = null, backIcon: String = "arrow-left",
    center: Boolean = false, big: Boolean = false, light: Boolean = false, actions: @Composable RowScope.() -> Unit = {},
) {
    val fg = if (light) Color.White else Dj.c.text
    Row(
        modifier.fillMaxWidth().heightIn(min = 54.dp).padding(start = if (onBack == null) 18.dp else 6.dp, end = 10.dp, top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (onBack != null) IconBtn(backIcon, onBack, tint = fg, description = t("Back", "واپس", "رجوع"))
        Column(Modifier.weight(1f), horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start) {
            Txt(title, if (big) Dj.type.headline else Dj.type.titleL, fg, maxLines = 1, align = if (center) TextAlign.Center else null)
            if (sub != null) Txt(sub, Dj.type.bodyS, if (light) Color.White.copy(alpha = 0.8f) else Dj.c.text2, maxLines = 1)
        }
        actions()
        if (center && onBack != null) Spacer(Modifier.width(0.dp))
    }
}

enum class Tab(val icon: String) { Home("house"), Quran("quran"), Learn("learn"), Worship("worship"), More("menu") }

@Composable
fun tabLabel(tab: Tab): String = when (tab) {
    Tab.Home -> t("Home", "ہوم", "الرئيسية")
    Tab.Quran -> t("Quran", "قرآن", "القرآن")
    Tab.Learn -> t("Learn", "سیکھیں", "تعلّم")
    Tab.Worship -> t("Worship", "عبادت", "العبادة")
    Tab.More -> t("More", "مزید", "المزيد")
}

@Composable
fun BottomNav(current: Tab, onSelect: (Tab) -> Unit) {
    val c = Dj.c
    Column(Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(0.dp), ambientColor = Color(0x22182118)).background(c.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Tab.entries.forEach { tab ->
                val on = tab == current
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Tab) { onSelect(tab) }.padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(Modifier.size(48.dp, 28.dp).clip(CircleShape).background(if (on) c.primaryTint else Color.Transparent), contentAlignment = Alignment.Center) {
                        DjIcon(tab.icon, 21.dp, if (on) c.primary else c.text3)
                    }
                    Txt(tabLabel(tab), if (on) Dj.type.labelS else Dj.type.caption, if (on) c.primary else c.text3, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** Sticky footer for primary actions. */
@Composable
fun Footer(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(Dj.c.bg).padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 8.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun DjSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = Dj.c.surface, shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { Box(Modifier.padding(top = 10.dp, bottom = 4.dp).size(40.dp, 5.dp).clip(CircleShape).background(Dj.c.border)) }) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 18.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ConfirmDialog(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, danger: Boolean = false, dismiss: String = t("Cancel", "منسوخ", "إلغاء")) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Dj.c.surface, shape = RoundedCornerShape(24.dp),
        title = { Txt(title, Dj.type.titleL, Dj.c.text) },
        text = { Txt(body, Dj.type.bodyM, Dj.c.text2) },
        confirmButton = { DjButton(confirm, onConfirm, style = if (danger) BtnStyle.Danger else BtnStyle.Primary, small = true) },
        dismissButton = { DjButton(dismiss, onDismiss, style = BtnStyle.Ghost, small = true) },
    )
}
