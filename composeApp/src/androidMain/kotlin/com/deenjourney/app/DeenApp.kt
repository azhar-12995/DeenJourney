package com.deenjourney.app

import android.app.Application
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.data.sync.SyncRepo
import com.deenjourney.app.di.initKoin
import com.deenjourney.app.platform.Channels
import com.deenjourney.app.platform.Scheduler
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.auth.auth
import com.deenjourney.app.data.sync.accountFirestore
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

class DeenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)
        initFirebase()
        initKoin(module { })
        Channels.create(this)
        KoinPlatform.getKoin().get<SyncRepo>().start()
        Scheduler.reschedule()
    }

    private fun initFirebase() {
        if (Firebase.apps(this).isEmpty()) {
            // No google-services.json yet: run against the local Firebase emulators (project "demo-deenjourney").
            Firebase.initialize(
                this, FirebaseOptions(applicationId = "1:000000000000:android:0000000000000000", apiKey = "demo-api-key-for-emulators-only", projectId = "demo-deenjourney"),
            )
        }
        if (BuildConfig.USE_EMULATORS) {
            // 10.0.2.2 = the host machine from the Android emulator
            runCatching { Firebase.auth.useEmulator("10.0.2.2", 9099) }
            runCatching { accountFirestore.useEmulator("10.0.2.2", 8080) }
        }
    }
}
