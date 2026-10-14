# Room rules
-keep class com.learner.invoicegenerator.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Retrofit and Gson
-keep class com.learner.invoicegenerator.ApiCalling.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Keep Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep ViewModels and UI state
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.learner.invoicegenerator.ui.** { *; }

# Signature Pad
-keep class com.github.gcacace.signaturepad.** { *; }
