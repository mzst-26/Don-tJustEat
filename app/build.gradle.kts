import org.gradle.testing.jacoco.tasks.JacocoReport
<<<<<<< HEAD
import java.io.File
=======
>>>>>>> origin/main
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    jacoco
}

<<<<<<< HEAD
// apply the google services plugin conditionally
val googleServicesFile = File(project.projectDir, "app/google-services.json")

if (googleServicesFile.exists()) {
    plugins.apply("com.google.gms.google-services")
}

=======
>>>>>>> origin/main
android {
    namespace = "com.example.dontjusteat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dontjusteat"
        minSdk = 24
        targetSdk = 36
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
<<<<<<< HEAD
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
=======
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
>>>>>>> origin/main
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
<<<<<<< HEAD

    packaging {
        resources {
            excludes += setOf(
                "**/* *",
                "**/* 2.*"
            )
        }
    }
=======
>>>>>>> origin/main
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.play.services.base)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.play.services.maps)
    implementation(libs.androidx.core)
    implementation(libs.androidx.recyclerview)
    implementation("com.github.bumptech.glide:glide:4.16.0")
<<<<<<< HEAD
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase services
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
=======
>>>>>>> origin/main

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.robolectric)
}

jacoco.toolVersion = "0.8.10"


tasks.register<JacocoReport>("jacocoTestReport") {

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${buildDir}/intermediates/javac/debug") {
            exclude("**/R.class")
        }
    )

    sourceDirectories.setFrom(files("src/main/java"))

    executionData.setFrom(
        fileTree(buildDir) {
            include("**/*.exec", "**/*.ec")
        }
    )
}
