plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python")
}

android {
    namespace = "com.csttine.utmn.lms.lmsnotifier"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.csttine.utmn.lms.lmsnotifier"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    flavorDimensions += "pyVersion"
    productFlavors {
        //create("py310") { dimension = "pyVersion" }
        create("py311") { dimension = "pyVersion" }
    }

}

chaquopy {
    defaultConfig {
        version = "3.11"
        //REPLACE TO YOUR OWN PATH
        buildPython("/home/l41n/Documents/Apps/Python-3.11.0/python")
        pip {
            install("requests")
        }
        pyc {
            src = false
        }
    }
    productFlavors {
        //getByName("py310") { version = "3.10" }
        getByName("py311") { version = "3.11" }
    }
    sourceSets { }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.preference)
    implementation(libs.recyclerview)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}