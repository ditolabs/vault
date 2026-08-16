# kotlinx.serialization needs its generated serializer classes kept
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ditolabs.pwvault.**$$serializer { *; }
-keepclassmembers class com.ditolabs.pwvault.** {
    *** Companion;
}
-keepclasseswithmembers class com.ditolabs.pwvault.** {
    kotlinx.serialization.KSerializer serializer(...);
}
