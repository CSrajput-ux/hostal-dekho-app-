plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.company.hostaldekho"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.company.hostaldekho"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Debug connects to FastAPI running on the development machine.
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/api/v1/\"")

        // Razorpay & Web Client IDs
        buildConfigField("String", "WEB_CLIENT_ID", "\"40726329823-37m63d5gl6ivaar4i41i8tismaasirhv.apps.googleusercontent.com\"")
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"rzp_test_YOUR_KEY_HERE\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // This deliberately cannot reach a real service. Set
            // PROD_API_BASE_URL in local/CI Gradle properties for each release.
            val productionApiUrl = providers.gradleProperty("PROD_API_BASE_URL")
                .orElse("https://api.hosteldekho.invalid/api/v1/")
                .get()
            buildConfigField("String", "BASE_URL", "\"$productionApiUrl\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable += setOf(
            "AutoboxingStateCreation",
            "MutableCollectionMutableState"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.4")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase Integration
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // AndroidX Credential Manager & Google Identity Services
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Play Integrity API
    implementation("com.google.android.play:integrity:1.4.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    // Payments
    implementation("com.razorpay:checkout:1.6.41")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
