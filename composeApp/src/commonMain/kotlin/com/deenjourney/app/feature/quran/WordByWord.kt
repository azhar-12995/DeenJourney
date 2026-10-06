package com.deenjourney.app.feature.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.data.net.QuranFoundation
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.Loaded
import org.koin.compose.koinInject

@Composable
fun WordByWord(key: String) {
    val remote = koinInject<QuranFoundation>(); val lang = LocalLang.current
    Loaded(key to lang, { remote.words(key, if (lang == Lang.UR) "urdu" else "english").also { check(it.isNotEmpty()) } }) { words ->
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            words.forEach { word -> Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ArabicText(word.arabic, Dj.type.arabicS); Txt(word.gloss, Dj.type.caption, Dj.c.text2)
            } }
        }
    }
}
