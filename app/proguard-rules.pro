# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep data classes
-keep class com.aithink.studycompanion.data.models.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.aithink.studycompanion.**$$serializer { *; }
-keepclassmembers class com.aithink.studycompanion.** {
    *** Companion;
}
-keepclasseswithmembers class com.aithink.studycompanion.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep RunAnywhere SDK
-keep class ai.runanywhere.** { *; }
-keep interface ai.runanywhere.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
