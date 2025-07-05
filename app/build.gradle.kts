import org.jetbrains.kotlin.resolve.featureDependencies

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleService)

}

android {
    namespace = "com.example.network"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.network"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures{
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
}

dependencies {

    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-installations:17.0.0")

    implementation ("com.hbb20:ccp:2.7.0")

    implementation("com.google.firebase:firebase-messaging-ktx")




    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.api-client:google-api-client:1.33.0")

    implementation(libs.client)
    implementation(libs.library)
    implementation (libs.glide)
    implementation(libs.firebaseStorage)
    implementation(libs.places)
    implementation(libs.mapas)


    implementation(libs.photoView)
    implementation(libs.firebaseAuth)
    implementation(libs.fireDataBase)
    implementation(libs.loginGoogle)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.messagin)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
}
dependencies {
    implementation(libs.firebase.messaging)
}
