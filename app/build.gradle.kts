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
		localeFilters += setOf("en")
	}

	signingConfigs {
		// the release key is provided by CI, local release builds stay unsigned
		val keystore = providers.environmentVariable("SIGNING_KEYSTORE").orNull
		if (keystore != null) {
			create("release") {
				storeFile = file(keystore)
				storePassword = providers.environmentVariable("SIGNING_PASSWORD").get()
				keyAlias = "unbird"
				keyPassword = providers.environmentVariable("SIGNING_PASSWORD").get()
			}
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			isDebuggable = false
			multiDexEnabled = false
			vcsInfo.include = false
			signingConfig = signingConfigs.findByName("release")
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
