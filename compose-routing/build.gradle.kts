
plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.compose)
}

/*
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    //alias(libs.plugins.kotlin.compose)
}
 */

android {
    namespace = "it.fast4x.compose.routing"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        //targetSdk = 34
    }

    buildTypes {
        create("kbuild") {
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
}
