package com.deenjourney.app.data.user

import androidx.room.Room
import androidx.room.RoomDatabase
import com.deenjourney.app.core.Platform
import com.deenjourney.app.data.sync.CloudEnvironment

actual fun userDbBuilder(): RoomDatabase.Builder<UserDb> =
    Room.databaseBuilder<UserDb>(name = Platform.filesDir() + "/user${CloudEnvironment.storageSuffix}.db")
