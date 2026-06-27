# ----- General Android & R8 -----
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ----- Kotlin -----
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.Intrinsics { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$ScheduledRunnable {
    *** run();
}

# ----- Hilt / Dagger -----
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.WithFragmentBindings class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.EntryPoint class *
-keep @dagger.hilt.components.SingletonComponent class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# ----- Jetpack Compose -----
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.material3.** { *; }

# ----- Room -----
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ----- Retrofit & OkHttp -----
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep @retrofit2.http.* class * { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ----- Gson -----
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ----- Kotlinx Serialization -----
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepclassmembers class com.kizunagateway.domain.model.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class com.kizunagateway.domain.model.** { *; }
-keepclassmembers class com.kizunagateway.domain.model.** {
    <fields>;
}

# ----- Domain Models -----
# Keep all domain models as they are often used for serialization/deserialization
-keep class com.kizunagateway.domain.model.** { *; }

# ----- Firebase -----
-keep class com.google.firebase.** { *; }

# ----- WorkManager -----
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ----- Ktor / Netty / Logging -----
-dontwarn io.netty.internal.tcnative.**
-dontwarn java.lang.management.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn org.slf4j.impl.**
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
