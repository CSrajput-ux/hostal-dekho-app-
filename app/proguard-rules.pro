# HostelDekho ProGuard Configuration Rules

# Firebase Keep Rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services Keep Rules
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Credential Manager & Google Identity Keep Rules
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

# Play Integrity Rules
-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**

# Retrofit & Gson Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.company.hostaldekho.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Hilt Rules
-keep class com.company.hostaldekho.di.** { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**
