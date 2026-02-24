# ProGuard rules for DrunkGuard

# Keep Room entities
-keep class com.traffic.drunkguard.data.model.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep TensorFlow Lite
-keep class org.tensorflow.** { *; }

# Keep MLKit
-keep class com.google.mlkit.** { *; }

# Keep iText
-keep class com.itextpdf.** { *; }

# Keep ZXing
-keep class com.google.zxing.** { *; }
