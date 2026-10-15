plugins {
	id("com.android.application")
}

android {
	compileSdk = 37
	compileSdkMinor = 0
	buildToolsVersion = "37.0.0"
	namespace = "org.nuclearfog.nitterify"

	defaultConfig {
		minSdk = 11
		targetSdk = 37
		versionCode = 8
		versionName = "2.1.5"
		applicationId = "org.nuclearfog.nitterify"
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
