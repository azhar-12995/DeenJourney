package com.deenjourney.app.data.sync

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.firestore.firestore

/** One selection for sync, correction reports, and account-data deletion. */
expect object CloudEnvironment {
    val databaseId: String
    val storageSuffix: String
}

val accountFirestore get() = Firebase.firestore(Firebase.app, CloudEnvironment.databaseId)
