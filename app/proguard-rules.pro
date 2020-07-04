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
-keep class com.aaron.yespdf.common.event.RecentPDFEvent { *; }

# AndroidPdfViewer
-keep class com.shockwave.**

# RealtimeBlurView
-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }

# GreenDAO
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties {*;}

# If you do not use SQLCipher:
#-dontwarn net.sqlcipher.database.**
# If you do not use RxJava:
#-dontwarn rx.**

# umeng
-keep class com.umeng.** {*;}

-keep class com.uc.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.zui.** {*;}
-keep class com.miui.** {*;}
-keep class com.heytap.** {*;}
-keep class a.** {*;}
-keep class com.vivo.** {*;}

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}
# tinker混淆规则
-dontwarn com.tencent.tinker.**
-keep class com.tencent.tinker.** { *; }

# ============================== 基本不用动区域 begin ==============================

# >>>>>>>>>>>> 基本指令区

# 代码混淆的压缩比例(0-7) , 默认为5 , 一般不需要改
-optimizationpasses 5

# 混淆后类名都小写 (windows最好加上 , 因为windows大小写敏感)
-dontusemixedcaseclassnames

# 指定不去忽略非公共的库的类(即混淆第三方, 第三方库可能自己混淆了 , 可在后面配置某些第三方库不混淆)
# 默认跳过，有些情况下编写的代码与类库中的类在同一个包下，并且持有包中内容的引用，此时就需要加入此条声明
-dontskipnonpubliclibraryclasses

# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers

# 不做预检验，preverify是proguard的四个步骤之一
# Android不需要preverify，去掉这一步可以加快混淆速度
-dontpreverify

# 有了verbose这句话，混淆后就会生成映射文件
# 包含有类名->混淆后类名的映射关系
# 然后使用printmapping指定映射文件的名称
-verbose
-printmapping proguardMapping.txt
# 指定混淆时采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不改变
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# 避免混淆泛型
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature

#抛出异常时保留源文件和代码行号
-keepattributes SourceFile,LineNumberTable


# >>>>>>>>>>>> 默认保留区
# 保留四大组件
-keep public class * extends android.app.Activity
# 保留就保证layout中定义的onClick方法不影响
# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application

-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
# 保留类名和native成员方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 枚举类不能被混淆
# # For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留自定义控件(继承自View)的setter、getter和构造方法
# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keep public class * extends android.view.View{
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    *** get*();
    void set*(***);
}
# 保留Parcelable序列化的类不能被混淆
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}
# 官方
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}


# 所有实现了 Serializable 接口的类及其成员都不进行混淆
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 对R文件下的所有类及其方法 , 都不能被混淆
#-keep class **.R$* {
# *;
#}
# 官方
-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**


# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}

# >>>>>>>>>>>> webview相关
-dontwarn android.webkit**

# WebView(可选)
-keepclassmembers class * extends android.webkit.WebView {
   public *;
}
# WebView的复杂操作
-keepclassmembers class * extends android.webkit.WebViewClient {
     public void *(android.webkit.WebView,java.lang.String,android.graphics.Bitmap);
     public boolean *(android.webkit.WebView,java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
     public void *(android.webkit.WebView,java.lang.String);
}

# 与JS交互
-keepattributes SetJavaScriptEnabled
-keepattributes JavascriptInterface

# 保留与JS交互接口 , API17+
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-dontwarn org.apache.**



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
