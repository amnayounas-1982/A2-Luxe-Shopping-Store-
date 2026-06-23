import com.android.build.gradle.internal.utils.immutableListBuilder

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.androidx.navigation.safe.args)


}

android {
    namespace = "com.example.snapstore"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.snapstore"
        minSdk = 24
        targetSdk = 35
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
}

dependencies{

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.database)
    implementation(libs.firebase.config)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // For scalable dp (sdp)
    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    // For scalable sp (ssp)
    implementation ("com.intuit.ssp:ssp-android:1.1.0")
    //For google sign in
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    // Jet pack navigation
    implementation ("androidx.navigation:navigation-fragment:2.7.7")
    implementation ("androidx.navigation:navigation-ui:2.7.7")
    // for firestore
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    // use for moving from one fragment to another
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    // for shape your image
    implementation ("com.google.android.material:material:<latest_version>")
    // for material card view
    implementation ("com.google.android.material:material:1.12.0")

    implementation ("com.google.firebase:firebase-config:22.0.0")
     // mujhay ghar jana ha mere bas ho gai ha or ek lamha yaha nhu betha ja rha
    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")









}