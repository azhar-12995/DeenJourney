package com.deenjourney.app.nav

import androidx.navigation.NavGraphBuilder
import com.deenjourney.app.feature.quran.*
import com.deenjourney.app.feature.worship.*
import com.deenjourney.app.feature.learn.*
import com.deenjourney.app.feature.tools.*
import com.deenjourney.app.feature.account.*

fun NavGraphBuilder.registerQuran() {
    screen<QuranHome> { QuranLibraryScreen(it.tab) }; screen<Reader> { QuranReaderScreen(it.sura, it.aya, it.play) }; screen<Player> { QuranPlayerScreen() }
    screen<Reciters> { ReciterPickerScreen() }; screen<Bookmarks> { QuranBookmarksScreen(it.tab) }; screen<Tafsir> { QuranTafsirScreen(it.sura, it.aya, it.tab) }
    screen<ShareAyah> { ShareAyahScreen(it.sura, it.aya) }; screen<QuranSearch> { QuranSearchScreen(it.q) }; screen<Qaida> { QaidaScreen() }; screen<Hifz> { HifzScreen(it.sura, it.from, it.to) }
}

fun NavGraphBuilder.registerWorship() {
    screen<PrayerTimes> { PrayerTimesScreen() }; screen<PrayerSettings> { PrayerSettingsScreen() }; screen<ChooseLocation> { ChooseLocationScreen(it.onboarding) }
    screen<Qibla> { QiblaScreen(it.tab) }; screen<GuideRoute> { GuideScreen(it.key) }; screen<LearnSalah> { LearnSalahScreen() }; screen<Kalimas> { KalimasScreen() }
    screen<Duas> { DuaLibraryScreen() }; screen<DuaCategory> { DuaLibraryScreen(it.id) }; screen<DuaDetail> { DuaDetailScreen(it.id) }; screen<Adhkar> { AdhkarScreen(it.kind) }
    screen<Tasbih> { TasbihScreen(it.tab) }; screen<SpecialPrayers> { SpecialPrayersScreen(it.tab) }
}

fun NavGraphBuilder.registerLearn() {
    screen<HadithDay> { HadithDayScreen() }; screen<HadithLibrary> { HadithLibraryScreen() }; screen<HadithBooks> { HadithBooksScreen(it.collection) }; screen<HadithList> { HadithListScreen(it.collection, it.book, it.title) }
    screen<HadithDetail> { HadithDetailScreen(it.collection, it.number, it.daily) }; screen<AajKiBaat> { ReflectionScreen() }; screen<Akhlaq> { AkhlaqScreen() }; screen<AkhlaqTopic> { AkhlaqScreen(it.id) }
    screen<Prophets> { ProphetsScreen() }; screen<Story> { StoryScreen(it.id, it.kids) }; screen<Seerah> { SeerahScreen() }; screen<Names> { NamesScreen(it.n) }
    screen<NamesGrid> { NamesScreen() }; screen<Roadmap> { RoadmapScreen() }; screen<Pillars> { PillarsScreen() }; screen<LessonRoute> { LessonScreen(it.id) }
    screen<QuizRoute> { QuizScreen(it.id) }; screen<LessonDone> { LessonDoneScreen(it.id, it.score, it.total) }; screen<Progress> { ProgressScreen() }
}

fun NavGraphBuilder.registerTools() {
    screen<Ramadan> { RamadanScreen() }; screen<QuranPlan> { QuranPlanScreen() }; screen<FastingGuide> { FastingGuideScreen() }; screen<RamadanTracker> { RamadanTrackerScreen() }
    screen<Zakat> { ZakatScreen() }; screen<ZakatLearn> { ZakatLearnScreen() }; screen<Hajj> { HajjScreen(it.tab) }; screen<HajjChecklist> { HajjChecklistScreen() }
    screen<HajjStage> { HajjStageScreen(it.guide, it.index) }; screen<Calendar> { CalendarScreen() }; screen<EventDetail> { EventDetailScreen(it.id, it.year) }
}

fun NavGraphBuilder.registerAccount() {
    screen<Profile> { ProfileScreen() }; screen<EditProfile> { EditProfileScreen() }; screen<SettingsRoute> { SettingsScreen() }
    screen<Downloads> { DownloadsScreen() }; screen<Privacy> { PrivacyScreen() }; screen<Help> { HelpScreen() }
    screen<Correction> { CorrectionScreen(it.ref) }; screen<About> { AboutScreen() }; screen<Saved> { SavedScreen() }
}
