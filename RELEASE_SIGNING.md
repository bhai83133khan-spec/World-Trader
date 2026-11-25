# Release signing setup

The debug APK workflow remains available for routine CI checks. A signed release APK is built only by a manual workflow dispatch or a version tag push (for example, `v1.0.0`).

## Required GitHub Actions secrets

In repository **Settings → Secrets and variables → Actions**, add:

- `ANDROID_SIGNING_KEY_BASE64`: the keystore file encoded as Base64, with no line breaks
- `ANDROID_SIGNING_STORE_PASSWORD`: keystore password
- `ANDROID_SIGNING_KEY_ALIAS`: key alias
- `ANDROID_SIGNING_KEY_PASSWORD`: key password

Never commit or share the keystore or its passwords. Keep at least two secure backups of the permanent keystore; future updates must use the same signing identity.

## Generate a permanent signing key (Termux)

Use a trusted Termux JDK that provides `keytool`:

```bash
pkg install openjdk-21
keytool -genkeypair -v -keystore oldworldtrader-release.jks -alias oldworldtrader -keyalg RSA -keysize 2048 -validity 10000
```

Choose and securely store the keystore and key passwords. Keep `oldworldtrader-release.jks` backed up in a secure location.

Encode the keystore for the GitHub secret:

```bash
base64 oldworldtrader-release.jks | tr -d '\n'
```

Copy the resulting Base64 text into the `ANDROID_SIGNING_KEY_BASE64` secret. Add the other three values as separate secrets. Do not put secrets into issues, commits, chat, or screenshots.

## Build and publish

- Use **Actions → Build Old World Trader APK → Run workflow** for a signed release artifact.
- Push a version tag such as `v1.0.0` to build, verify, and publish a GitHub Release.
- The release job runs unit tests, `assembleRelease`, signature verification with `apksigner`, and checks the APK package name before upload/publication.

If signing secrets are missing, the release job fails before building; this is intentional so an unsigned APK is never mislabeled as a release.
