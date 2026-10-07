package com.deenjourney.app.data.sync

actual object CloudEnvironment {
    actual val databaseId: String = "(default)"
    actual val storageSuffix: String = if (kotlin.native.Platform.isDebugBinary) "-debug" else ""
}
