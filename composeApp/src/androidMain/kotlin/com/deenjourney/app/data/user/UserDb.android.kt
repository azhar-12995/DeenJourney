package com.deenjourney.app.data.user

import androidx.room.Room
import androidx.room.RoomDatabase
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.data.sync.CloudEnvironment

actual fun userDbBuilder(): RoomDatabase.Builder<UserDb> {
    val ctx = AndroidPlatform.context
    return Room.databaseBuilder<UserDb>(ctx, ctx.getDatabasePath("user${CloudEnvironment.storageSuffix}.db").absolutePath)
}
