plugins {
    id("com.android.application") version "8.5.2"
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

android {
    namespace = "com.recovereasy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.recovereasy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { isMinifyEnabled = false }
    }

    // ✅ เปิดใช้ Compose
    buildFeatures {
        compose = true
        // (ถ้ายังมีหน้าที่ใช้ ViewBinding อยู่ด้วย จะเปิด viewBinding = true ก็ได้)
        // viewBinding = true
    }
    composeOptions {
        // เวอร์ชัน compiler สำหรับ Kotlin 1.9.24
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose ใช้ BOM จัดเวอร์ชันให้สัมพันธ์กันทั้งหมด
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // ✅ รูปภาพใน Compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    // (ถ้าในโปรเจกต์เดิมยังมีการใช้ ImageView.load อยู่ด้วย)
    implementation("io.coil-kt:coil:2.6.0")

    // AndroidX พื้นฐาน
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // สำหรับ preview/debug เท่านั้น
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
