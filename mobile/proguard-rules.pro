# Add project specific ProGuard rules here.

-keepclassmembers class * {
    @androidx.annotation.Keep <init>(...);
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <fields>;
}

##### MOSHI ######

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}

-keep @com.squareup.moshi.JsonQualifier @interface *

-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <fields>;
    **[] values();
}

-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}

-keepclassmembers class * {
  @com.squareup.moshi.FromJson <methods>;
  @com.squareup.moshi.ToJson <methods>;
}
