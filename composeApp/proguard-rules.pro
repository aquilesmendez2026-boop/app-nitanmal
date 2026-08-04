# ── kotlinx-serialization ──
# Los modelos se (de)serializan por reflexión de sus serializers generados.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.nitanmal.app.**$$serializer { *; }
-keepclassmembers class com.nitanmal.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.nitanmal.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Ktor (motor OkHttp) ──
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**

# ── Credential Manager / Google Sign-In ──
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }

# Compacta el DEX moviendo las clases ofuscadas al paquete raiz
-repackageclasses

# Trazas de pila legibles en Play Console
-keepattributes SourceFile,LineNumberTable
