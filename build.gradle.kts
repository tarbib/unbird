buildscript {
	repositories {
		mavenCentral()
		maven("https://plugins.gradle.org/m2/")
		maven("https://maven.google.com")
		google()
	}

	dependencies {
		classpath("com.android.tools.build:gradle:9.1.0")
	}
}

allprojects {
	repositories {
		mavenCentral()
		google()
		maven("https://jitpack.io")
	}
}

tasks.register<Delete>("clean") {
	delete(rootProject.layout.buildDirectory)
}
