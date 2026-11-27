# Add this to suppress warnings from Google API client libraries
-keep class com.google.api.** { *; }
-keep class com.google.apis.** { *; }
-keep class com.google.auth.** { *; }
-keep class com.google.oauth2.** { *; }
-keep class com.google.gson.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# Hilt
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}

# DataStore
-keepclassmembers class * implements com.google.protobuf.MessageLite { *; }

# OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Google Sign-In / Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }

# General
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
