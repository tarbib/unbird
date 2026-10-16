plugins {
	id("com.android.application") version "9.1.0"
}

android {
	namespace = "io.github.tarbib.unbird"
	compileSdk = 37
	compileSdkMinor = 0
	buildToolsVersion = "37.0.0"

	defaultConfig {
		applicationId = "io.github.tarbib.unbird"
		minSdk = 29
		targetSdk = 37
		// numbered by the GitHub Actions run, so every published APK is an update of the previous one
		val build = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
		versionCode = build
		versionName = "1.0.$build"
	}

	signingConfigs {
		// optional release key, without it release builds are signed with the debug key
		System.getenv("SIGNING_KEYSTORE")?.let { keystore ->
			create("release") {
				storeFile = file(keystore)
				storePassword = System.getenv("SIGNING_PASSWORD")
				keyAlias = "unbird"
				keyPassword = System.getenv("SIGNING_PASSWORD")
			}
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
		}
	}
}

dependencies {
	implementation("com.google.android.material:material:1.14.0")
	implementation("androidx.constraintlayout:constraintlayout:2.2.2")
}
