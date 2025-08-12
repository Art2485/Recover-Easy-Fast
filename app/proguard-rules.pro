# --------- Base safety ---------
# เก็บเมทาดาทา Kotlin (กันรีเฟลกชันพัง)
-keep class kotlin.Metadata { *; }

# ไม่ทำให้ชื่อ R classes หาย (กันรีซอร์สพัง)
-keep class **.R$* { *; }

# เก็บชื่อ Activity/Application ตามที่อ้างใน Manifest
-keep class **.MainActivity { *; }
-keep class com.recovereasy.** { *; }

# Parcelable / Serializable creators
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class ** implements java.io.Serializable { *; }

# RecyclerView adapters + viewholders
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }

# MediaStore inner classes / column names (กันรีเฟลกชัน/สตริงพังเวลาคิวรี)
-keep class android.provider.MediaStore$** { *; }

# --------- Imaging libs (เผื่อใช้ในอนาคต) ---------
# Coil (ถ้ามี)
-keep class coil.** { *; }
-dontwarn coil.**
# Glide (ถ้ามี)
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# --------- General ---------
# ลด warning ที่ไม่เกี่ยว
-dontnote kotlinx.**
-dontwarn javax.annotation.**
