package com.deenjourney.app.nav

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.deenjourney.app.design.Tab
import kotlinx.serialization.Serializable

/** Every destination (ids in comments match the Figma screen ids). */
@Serializable sealed interface R

// A · start & account
@Serializable data object Splash : R                    // A01
@Serializable data object Welcome : R                   // A02
@Serializable data object SignIn : R                    // A03
@Serializable data object SignUp : R                    // A04
@Serializable data object Forgot : R                    // A05
@Serializable data class CheckEmail(val email: String) : R // A06
@Serializable data class PrayerSetup(val fromSettings: Boolean = false) : R // A07
@Serializable data object NotifSetup : R                // A08
// B · family onboarding
@Serializable data class FamilySetup(val onboarding: Boolean = true) : R // B01
@Serializable data class AgeLevel(val profileId: String, val onboarding: Boolean = true) : R // B02
@Serializable data class Goals(val onboarding: Boolean = true) : R // B03
@Serializable data class AddMember(val kind: String = "child", val profileId: String? = null) : R // B04
@Serializable data object AllSet : R                    // B05
// C · hubs
@Serializable data object Home : R                      // C01
@Serializable data object Notifications : R             // C02
@Serializable data object LearnHub : R                  // C04
@Serializable data object WorshipHub : R                // C05
@Serializable data object More : R                      // C06
@Serializable data class Search(val q: String = "") : R // C07
// D · Quran
@Serializable data class QuranHome(val tab: Int = 0) : R // D01/D02
@Serializable data class Reader(val sura: Int, val aya: Int = 1, val play: Boolean = false) : R // D03
@Serializable data object Player : R                    // D06
@Serializable data object Reciters : R                  // D07
@Serializable data class Bookmarks(val tab: Int = 0) : R // D08
@Serializable data class Tafsir(val sura: Int, val aya: Int, val tab: Int = 0) : R // D10
@Serializable data class ShareAyah(val sura: Int, val aya: Int) : R // D11
@Serializable data class QuranSearch(val q: String = "") : R // D12
@Serializable data object Qaida : R                     // D13
@Serializable data class Hifz(val sura: Int = 1, val from: Int = 1, val to: Int = 7) : R // D14
// E · worship
@Serializable data object PrayerTimes : R               // E01
@Serializable data object PrayerSettings : R            // E02
@Serializable data class ChooseLocation(val onboarding: Boolean = false) : R // E03
@Serializable data class Qibla(val tab: Int = 0) : R    // E04/E05
@Serializable data class GuideRoute(val key: String) : R // E06/E07/E16 (wudu, ghusl, janazah, …)
@Serializable data object LearnSalah : R                // E08
@Serializable data object Kalimas : R                   // E09
@Serializable data object Duas : R                      // E10
@Serializable data class DuaCategory(val id: String) : R
@Serializable data class DuaDetail(val id: String) : R  // E11
@Serializable data class Adhkar(val kind: String = "morning") : R // E12
@Serializable data class Tasbih(val tab: Int = 0) : R   // E13/E14
@Serializable data class SpecialPrayers(val tab: Int = 0) : R // E15
// F · knowledge
@Serializable data object HadithDay : R                 // F01
@Serializable data object HadithLibrary : R             // F02
@Serializable data class HadithBooks(val collection: String) : R // F03
@Serializable data class HadithList(val collection: String, val book: Int, val title: String) : R
@Serializable data class HadithDetail(val collection: String, val number: Int, val daily: String? = null) : R // F04
@Serializable data object AajKiBaat : R                 // F05
@Serializable data object Akhlaq : R                    // F06
@Serializable data class AkhlaqTopic(val id: String) : R // F07
@Serializable data object Prophets : R                  // F08
@Serializable data class Story(val id: String, val kids: Boolean = false) : R // F09
@Serializable data object Seerah : R                    // F10
@Serializable data class Names(val n: Int = 1) : R      // F11
@Serializable data object NamesGrid : R                 // F12
@Serializable data object Roadmap : R                   // F13
@Serializable data object Pillars : R                   // F14
@Serializable data class LessonRoute(val id: String) : R // F15
@Serializable data class QuizRoute(val id: String) : R  // F16
@Serializable data class LessonDone(val id: String, val score: Int, val total: Int) : R // F17
@Serializable data object Progress : R                  // F18
// G · family & kids
@Serializable data object KidsSmall : R                 // G01
@Serializable data object KidsHome : R                  // G02
@Serializable data object ParentDashboard : R           // G03
@Serializable data class ChildSafe(val profileId: String) : R // G04
// H · seasons & tools
@Serializable data object Ramadan : R                   // H01
@Serializable data object QuranPlan : R                 // H02
@Serializable data object FastingGuide : R              // H03
@Serializable data object RamadanTracker : R            // H04
@Serializable data object Zakat : R                     // H05
@Serializable data object ZakatLearn : R                // H06
@Serializable data class Hajj(val tab: Int = 0) : R     // H07
@Serializable data object HajjChecklist : R             // H08
@Serializable data class HajjStage(val guide: String, val index: Int) : R // H09
@Serializable data object Calendar : R                  // H10
@Serializable data class EventDetail(val id: String, val year: Int) : R // H11
// I · account
@Serializable data object Profile : R                   // I01
@Serializable data object EditProfile : R               // I02
@Serializable data object SettingsRoute : R             // I03
@Serializable data object Downloads : R                 // I04
@Serializable data object Privacy : R                   // I05
@Serializable data object Help : R                      // I07
@Serializable data class Correction(val ref: String = "") : R // I08
@Serializable data object About : R                     // I09
@Serializable data object Saved : R                     // I10

/** Navigation helper shared by all screens. */
class Navigator(val nav: NavHostController) {
    fun go(r: R) = nav.navigate(r) { launchSingleTop = true }
    fun back() { if (!nav.popBackStack()) Unit }
    /** Replace the whole stack (e.g. after sign-in). */
    fun reset(r: R) = nav.navigate(r) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
    fun tab(t: Tab) {
        val r: R = when (t) { Tab.Home -> Home; Tab.Quran -> QuranHome(); Tab.Learn -> LearnHub; Tab.Worship -> WorshipHub; Tab.More -> More }
        nav.navigate(r) { popUpTo(Home) { saveState = true }; launchSingleTop = true; restoreState = true }
    }
    /** Opens a screen by its Figma id or a "screen:XX" link from content (FAQ, pillars …). */
    fun open(link: String?) {
        val id = link?.removePrefix("screen:") ?: return
        val r: R? = when (id) {
            "E01" -> PrayerTimes; "E02" -> PrayerSettings; "E03" -> ChooseLocation(); "E04" -> Qibla(); "E08" -> LearnSalah; "E10" -> Duas
            "H01" -> Ramadan; "H05" -> Zakat; "H07" -> Hajj(); "H10" -> Calendar; "I03" -> SettingsRoute; "I04" -> Downloads; "I05" -> Privacy
            "I08" -> Correction(); "I09" -> About; "G04" -> ParentDashboard; "B04" -> AddMember(); "D01" -> QuranHome(); "C01" -> Home
            else -> if (id.startsWith("lesson:")) LessonRoute(id.removePrefix("lesson:")) else null
        }
        r?.let { go(it) }
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not provided") }
