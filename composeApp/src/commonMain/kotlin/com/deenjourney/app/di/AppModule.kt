package com.deenjourney.app.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.db.BundledDb
import com.deenjourney.app.data.db.DbInstaller
import com.deenjourney.app.data.db.ReadOnlyDb
import com.deenjourney.app.data.hadith.HadithStore
import com.deenjourney.app.data.location.CitiesRepo
import com.deenjourney.app.data.net.PriceRemote
import com.deenjourney.app.data.net.QuranFoundation
import com.deenjourney.app.data.net.buildHttp
import com.deenjourney.app.data.prayer.PrayerClock
import com.deenjourney.app.data.quran.QuranRepo
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.sync.SyncRepo
import com.deenjourney.app.data.user.UserDb
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.data.user.userDbBuilder
import com.deenjourney.app.platform.CompassService
import com.deenjourney.app.platform.LocationService
import com.deenjourney.app.platform.RecitationPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { SettingsRepo(get()) }
    single { DbInstaller(get()) }
    single(named("quran")) { val i = get<DbInstaller>(); ReadOnlyDb { i.ensure(BundledDb.QURAN) } }
    single(named("cities")) { val i = get<DbInstaller>(); ReadOnlyDb { i.ensure(BundledDb.CITIES) } }
    single { QuranRepo(get(named("quran"))) }
    single { CitiesRepo(get(named("cities"))) }
    single { ContentRepo() }
    single<UserDb> { userDbBuilder().setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).fallbackToDestructiveMigrationOnDowngrade(true).build() }
    single { UserRepo(get(), get(), get()) }
    single { AuthRepo(get()) }
    single { SyncRepo(get(), get(), get(), get()) }
    single { buildHttp() }
    single { QuranFoundation(get()) }
    single { PriceRemote(get()) }
    single { HadithStore(get()) }
    single { LocationService() }
    single { CompassService() }
    single { RecitationPlayer() }
    single { PrayerClock(get()) }
}

fun initKoin(platform: Module = module { }) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin { modules(appModule, platform) }
}
