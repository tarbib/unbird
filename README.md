# Unbird

## Description

This Android app simply redirects X/Twitter links to a more privacy friendly <b>Nitter</b> instance.
Choose the instance from the healthy ones listed by [status.d420.de](https://status.d420.de/) or enter a custom one, nitter.net is used until then.
Lightweight: the only permission is internet access, used just to load that list when you ask for it.

## Download

Every push to `main` publishes the APK as a new release that replaces the previous one: [download the APK](https://github.com/tarbib/unbird/releases/latest/download/unbird.apk). Requires Android 10 or newer.
To get the updates, add `https://github.com/tarbib/unbird` to [Obtainium](https://github.com/ImranR98/Obtainium).

## Build

With Gradle 9.3.1, JDK 21 and the Android SDK:

```sh
gradle assembleRelease
```

The APK is signed with the release key when `SIGNING_KEYSTORE` (key alias `unbird`) and `SIGNING_PASSWORD` are set, otherwise with the debug key.
GitHub Actions reads them from the `SIGNING_KEYSTORE_BASE64` and `SIGNING_PASSWORD` repository secrets and only publishes APKs signed with the release key, since an update must be signed with the same key as the installed app.
The version of a build of GitHub Actions is 1.0 followed by the run number.

## Credits

Unbird is a modified version of [Nitterify](https://codeberg.org/nuclearfog/Nittterify) by nuclearfog.
Changes since October 2026: instance chooser based on status.d420.de with a custom instance option, Material 3 design, rewrite in Kotlin, new name and application ID, GitHub Actions builds.

## License

[GPL-3.0](LICENSE)
