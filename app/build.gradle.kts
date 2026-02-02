plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.example.runnconnect"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.runnconnect"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {

  implementation(libs.appcompat)
  implementation(libs.material)
  implementation(libs.constraintlayout)
  implementation(libs.lifecycle.livedata.ktx)
  implementation(libs.lifecycle.viewmodel.ktx)
  implementation(libs.navigation.fragment)
  implementation(libs.navigation.ui)
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.converter.scalars)
  implementation(libs.legacy.support.v4)
  implementation(libs.browser)
  implementation(libs.glide)
  implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
    testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
}