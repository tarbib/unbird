plugins {
	id("com.android.application")
}

android {
	compileSdk = 37
	compileSdkMinor = 0
	buildToolsVersion = "37.0.0"
	namespace = "io.github.tarbib.unbird"

	defaultConfig {
		minSdk = 11
		targetSdk = 37
		versionCode = 1
		versionName = "1.0.0"
		applicationId = "io.github.tarbib.unbird"
	}

	androidResources {
		localeFilters += setOf("en", "de")
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			isDebuggable = false
			multiDexEnabled = false
			vcsInfo.include = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}

	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}

	packaging {
		resources {
			excludes += setOf("/META-INF/CHANGES", "/META-INF/DEPENDENCIES", "/META-INF/README.md")
		}
	}
}

tasks.configureEach {
	if (name.contains("ArtProfile")) {
		enabled = false
	}
}
