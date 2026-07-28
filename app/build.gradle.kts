import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
}

// The on-device LLM model (.litertlm) is too large for git (see .gitignore) and is
// loaded from app assets at runtime by LocalLlmService. Rather than committing it,
// drop the model file under a local `models/` directory (repo root, git-ignored) —
// or point MODEL_DIR at another local path — and this task stages it into
// src/main/assets before every build. If no model is found, the build still
// succeeds; LocalLlmService falls back to regex-based parsing at runtime.
val localModelDir = System.getenv("MODEL_DIR")
    ?: (project.findProperty("modelDir") as String?)
    ?: "${rootProject.rootDir}/models"

tasks.register<Copy>("copyLocalModel") {
    val modelDir = file(localModelDir)
    val assetsDir = file("src/main/assets")

    from(modelDir) { include("*.litertlm") }
    into(assetsDir)

    onlyIf {
        val hasModel = modelDir.listFiles { f -> f.extension == "litertlm" }?.isNotEmpty() == true
        if (!hasModel) {
            logger.lifecycle(
                "[copyLocalModel] No .litertlm file found in ${modelDir.path} — " +
                    "skipping on-device model bundling; app will use fallback parsing."
            )
        }
        hasModel
    }

    doFirst { assetsDir.mkdirs() }
}

tasks.named("preBuild") {
    dependsOn("copyLocalModel")
}

android {
    namespace = "com.nextgen.expend"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.nextgen.expend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    aaptOptions {
        noCompress("litertlm")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.firebase.ai)

    // Room persistence for local transaction storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // LiteRT LLM – use litertlm-android (0.14.0+) only; do NOT add libs.litertlm (alpha05) as it causes duplicate class conflicts
    implementation("com.google.ai.edge.litertlm:litertlm-android:latest.release")

    // Vico Charts – banking-grade chart library for Compose
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
}