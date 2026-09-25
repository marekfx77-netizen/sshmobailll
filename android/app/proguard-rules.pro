-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.jcraft.jsch.** { *; }
-dontwarn com.jcraft.jsch.**
