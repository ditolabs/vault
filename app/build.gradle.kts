plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ditolabs.pwvault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ditolabs.pwvault"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Icon rule (non-negotiable, see DESIGN.md / CLAUDE.md): no
    // material-icons-extended. As of this pass we dropped material-icons-
    // core too — every glyph the app actually needs (copy, eye, eye-off,
    // back arrow, lock, warning) is hand-drawn in PwVaultIcons.kt, so
    // there's no icon-font dependency left to accidentally bloat.
    implementation("androidx.compose.material3:material3")
    // XML theme parent (Theme.Material3.DayNight.NoActionBar) used by
    // res/values/themes.xml for the cold-start launch theme — this comes
    // from the View-system Material Components lib, not from Compose
    // material3. Missing this caused the AAPT "resource ... not found" build
    // failure (Compose material3 alone doesn't ship XML styles).
    implementation("com.google.android.material:material:1.12.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
