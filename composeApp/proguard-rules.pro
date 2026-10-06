# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.deenjourney.app.**$$serializer { *; }
-keepclassmembers class com.deenjourney.app.** { *** Companion; kotlinx.serialization.KSerializer serializer(...); }
# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
# Firebase / Ktor / Adhan
-keep class com.google.firebase.** { *; }
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-keep class com.batoulapps.adhan2.** { *; }
