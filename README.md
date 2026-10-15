# Unbird

## Description

This Android app simply redirects X/Twitter links to a more privacy friendly <b>Nitter</b> instance.
Choose the instance from the healthy ones listed by [status.d420.de](https://status.d420.de/), nitter.net is used until then.
Lightweight: the only permission is internet access, used just to load that list when you ask for it.

## Download

The latest APK is attached to the newest [release](https://github.com/tarbib/unbird/releases/latest).

## Build

```sh
./gradlew assembleRelease
```

Release builds are signed when the `SIGNING_KEYSTORE` (path to the keystore, key alias `unbird`) and `SIGNING_PASSWORD` environment variables are set.
GitHub Actions builds every push and publishes a release for every `v*` tag, using the `SIGNING_KEYSTORE_BASE64` and `SIGNING_PASSWORD` repository secrets.

## Credits

Unbird is a modified version of [Nitterify](https://codeberg.org/nuclearfog/Nittterify) by nuclearfog.
Changes since October 2026: instance chooser based on status.d420.de replacing the manual instance setting, rewrite in Kotlin, new name and application ID, GitHub Actions builds.

## License

[GPL-3.0](LICENSE)
