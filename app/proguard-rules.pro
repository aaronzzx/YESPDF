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

# YES PDF!
-keep class com.aaron.yespdf.main.RecentPDFEvent {*;}

# AndroidPdfViewer
-keep class com.shockwave.**

# RealtimeBlurView
-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }

# ============================== 基本不动区域 end ==============================

# ============================== 框架中使用到的第三方依赖 begin ==============================

# >>>>>>>>>>>> squareup全家桶
-keep public class com.squareup.** { *;}
-dontwarn com.squareup**

# >>>>>>>>>>>> alibaba全家桶
-keep public class com.alibaba.** { *;}
-dontwarn com.alibaba**

# >>>>>>>>>>>> okhttp & okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# >>>>>>>>>>>> Gson
-keep public class com.google.gson.**
-keep public class com.google.gson.** {public private protected *;}
# 需要在各自工程下的混淆规则中,将bean实体类keep掉

# >>>>>>>>>>>> EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# >>>>>>>>>>>> Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# >>>>>>>>>>>> butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# >>>>>>>>>>>> agentweb
-keep class com.just.agentweb.** {
    *;
}
-dontwarn com.just.agentweb.**
# ============================== 框架中使用到的第三方依赖 end ==============================
