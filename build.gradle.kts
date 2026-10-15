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
		minSdk = 11
		targetSdk = 37
		versionCode = 1
		versionName = "1.0.0"
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
