# ── Savio ProGuard / R8 Rules ────────────────────────────────────────────────

# Kotlin serialization — necessario per i DTO JSON
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.paolomarchionetti.savio.**$$serializer { *; }
-keepclassmembers class com.paolomarchionetti.savio.** {
    *** Companion;
}
-keepclasseswithmembers class com.paolomarchionetti.savio.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room — entità e DAO non devono essere offuscati
-keep class com.paolomarchionetti.savio.data.local.entity.** { *; }
-keep class com.paolomarchionetti.savio.data.local.dao.** { *; }
-keep class com.paolomarchionetti.savio.data.local.db.** { *; }

# Hilt — generato a compile time, non toccare
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# DTO per la rete (se aggiunti in futuro)
-keep class com.paolomarchionetti.savio.data.assets.dto.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Compose — non offuscare i Composable
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Regola generale: non rimuovere classi con @HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
