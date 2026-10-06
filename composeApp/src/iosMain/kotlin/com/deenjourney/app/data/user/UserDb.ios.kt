package com.deenjourney.app.data.user

import androidx.room.Room
import androidx.room.RoomDatabase
import com.deenjourney.app.core.Platform

actual fun userDbBuilder(): RoomDatabase.Builder<UserDb> =
    Room.databaseBuilder<UserDb>(name = Platform.filesDir() + "/user.db")
