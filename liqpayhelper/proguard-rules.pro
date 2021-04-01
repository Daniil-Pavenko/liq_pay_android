# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable

#Models
-keep class com.dansdev.liqpayhelper.model.** { *; }
-keep class com.dansdev.liqpayhelper.constant.** { *; }
-keep class com.dansdev.liqpayhelper.LiqPay { *; }
-keepclasseswithmembernames class com.dansdev.liqpayhelper.api.** { *; }
-keepclasseswithmembernames class com.dansdev.liqpayhelper.model.** { *; }

-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# Annotatinos
-keep public class com.google.errorprone.annotations.** { *; }
-keep public class androidx.annotation.** { *; }

# HTTP
-keep class com.google.gson.** { *; }
-keep public class com.google.gson.** {public private protected *;}
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn com.squareup.okhttp.*
-dontwarn javax.xml.stream.**
-dontwarn com.google.appengine.**
-dontwarn java.nio.file.**
-dontwarn org.codehaus.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers enum * {
    public *;
}
