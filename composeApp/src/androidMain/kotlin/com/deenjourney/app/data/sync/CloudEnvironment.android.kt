package com.deenjourney.app.data.sync

import com.deenjourney.app.BuildConfig

actual object CloudEnvironment {
    actual val databaseId: String = BuildConfig.FIRESTORE_DATABASE_ID
    actual val storageSuffix: String = if (BuildConfig.DEBUG) "-debug" else ""
}
