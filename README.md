# Unbird

## Description

This Android app simply redirects X/Twitter links to a more privacy friendly <b>Nitter</b> instance.
Choose the instance from the healthy ones listed by [status.d420.de](https://status.d420.de/), nitter.net is used until then.
Lightweight: the only permission is internet access, used just to load that list when you ask for it.

## Download

Every push to `main` builds the APK and publishes it as a release: [latest APK](https://github.com/tarbib/unbird/releases/latest/download/unbird.apk).

## Build

With Gradle 9.3.1, JDK 21 and the Android SDK:

```sh
gradle assembleRelease
```

The APK is signed with the release key when `SIGNING_KEYSTORE` (key alias `unbird`) and `SIGNING_PASSWORD` are set, otherwise with the debug key.
GitHub Actions reads them from the optional `SIGNING_KEYSTORE_BASE64` and `SIGNING_PASSWORD` repository secrets.

## Credits

Unbird is a modified version of [Nitterify](https://codeberg.org/nuclearfog/Nittterify) by nuclearfog.
Changes since October 2026: instance chooser based on status.d420.de replacing the manual instance setting, rewrite in Kotlin, new name and application ID, GitHub Actions builds.

## License

[GPL-3.0](LICENSE)
