plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "hu.bme.aut.crypto_casino_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "hu.bme.aut.crypto_casino_android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
            buildConfigField("String", "ETHEREUM_RPC_URL", "\"http://10.0.2.2:8545\"")
            buildConfigField("String", "CASINO_TOKEN_ADDRESS", "\"0x5FbDB2315678afecb367f032d93F642f64180aa3\"")
            buildConfigField("String", "CASINO_VAULT_ADDRESS", "\"0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            // Exclude META-INF files
            excludes.addAll(listOf(
                "META-INF/**",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",

                // Exclude specific Bouncy Castle files
                "META-INF/versions/9/module-info.class",

                // Exclude signature files
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA",

                // JNI files that might cause conflicts
                "lib/*/libRSSupport.so",
                "lib/*/librsjni.so",
                "lib/*/librsjni_androidx.so"
            ))

            // Handle duplicate files (pick first occurrence)
            pickFirsts.addAll(listOf(
                // Handle duplicate service providers
                "META-INF/services/javax.annotation.processing.Processor",
                "META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler",
                "META-INF/services/kotlinx.coroutines.internal.MainDispatcherFactory",
                "META-INF/services/org.xmlpull.v1.XmlPullParserFactory"
            ))

            // Merge specific files instead of picking first
            merges.addAll(listOf(
                "META-INF/services/io.grpc.LoadBalancerProvider",
                "META-INF/services/io.grpc.NameResolverProvider"
            ))
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coroutines)
    implementation(libs.core)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
