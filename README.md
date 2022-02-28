# Usage
Add the desired library as following
```kotlin
implementation("me.proton.core:<name-of-the-lib>:<version>")
```
where `<name-of-the-lib>` reflect the one listed below, on lowercase, with `-` instead of spaces
`Util Android Shared Preferences: **0.1**` can be resolved as `me.proton.core:util-android-share-preferences:0.1`

Where there are all or some of `domain`, `data`, `presentation`, they can be imported together with the parent module.
```kotlin
implementation("~:network-domain:~")
implementation("~:network-data:~")
```
or
```kotlin
implementation("~:network:~")
```
while the first one is suitable for multi-module projects, the latter is the suggested solution for monolithic clients.
    
# Release

## Core libraries
Release process is based on [trunk branch for release process](https://trunkbaseddevelopment.com/branch-for-release/).
Release is done by the CI. To trigger a release for version `X.Y.Z`, just push a branch named `release/libs/X.Y.Z`.
When the release is successfully done:
* A message is post on internal communication tool using the content of the [`CHANGELOG.MD`](./CHANGELOG.md) under the entry `## [X.Y.Z].
* A tag `release/libs/X.Y.Z` is created from the commit used to do the release.

Before triggering a release (by pushing a release branch), `CHANGELOG.MD` should be updated by adding a entry `## [X.Y.Z]` with the content cut/paste from the entry `## [Unreleased]`.
Release should be made from `main` branch.

## Version
- Stable release `X.Y.Z` are published on `MavenCentral`.
- Snapshot from every commit on the `main` branch are available using version `main-SNAPSHOT` under `https://s01.oss.sonatype.org/content/repositories/snapshots/` repo.

## Coordinates
Core libraries coordinates can be found under [coordinates section](#coordinates)

| Coordinates            |
|------------------------|
|me.proton.core:account|
|me.proton.core:account-data|
|me.proton.core:account-domain|
|me.proton.core:account-manager|
|me.proton.core:account-manager-dagger|
|me.proton.core:account-manager-data|
|me.proton.core:account-manager-data-db|
|me.proton.core:account-manager-domain|
|me.proton.core:account-manager-presentation|
|me.proton.core:account-manager-presentation-compose|
|me.proton.core:auth|
|me.proton.core:auth-data|
|me.proton.core:auth-domain|
|me.proton.core:auth-presentation|
|me.proton.core:contact|
|me.proton.core:contact-data|
|me.proton.core:contact-domain|
|me.proton.core:contact-hilt|
|me.proton.core:country|
|me.proton.core:country-data|
|me.proton.core:country-domain|
|me.proton.core:country-presentation|
|me.proton.core:crypto|
|me.proton.core:crypto-android|
|me.proton.core:crypto-common|
|me.proton.core:crypto-validator|
|me.proton.core:crypto-validator-dagger|
|me.proton.core:crypto-validator-data|
|me.proton.core:crypto-validator-domain|
|me.proton.core:crypto-validator-presentation|
|me.proton.core:data|
|me.proton.core:data-room|
|me.proton.core:domain|
|me.proton.core:event-manager|
|me.proton.core:event-manager-data|
|me.proton.core:event-manager-domain|
|me.proton.core:feature-flag|
|me.proton.core:feature-flag-data|
|me.proton.core:feature-flag-domain|
|me.proton.core:feature-flag-dagger|
|me.proton.core:human-verification|
|me.proton.core:human-verification-data|
|me.proton.core:human-verification-domain|
|me.proton.core:human-verification-presentation|
|me.proton.core:key|
|me.proton.core:key-data|
|me.proton.core:key-domain|
|me.proton.core:label|
|me.proton.core:label-dagger|
|me.proton.core:label-data|
|me.proton.core:label-domain|
|me.proton.core:mail-message|
|me.proton.core:mail-message-data|
|me.proton.core:mail-message-domain|
|me.proton.core:mail-settings|
|me.proton.core:mail-settings-data|
|me.proton.core:mail-settings-domain|
|me.proton.core:metrics|
|me.proton.core:metrics-dagger|
|me.proton.core:metrics-data|
|me.proton.core:metrics-domain|
|me.proton.core:network|
|me.proton.core:network-dagger|
|me.proton.core:network-data|
|me.proton.core:network-domain|
|me.proton.core:payment|
|me.proton.core:payment-data|
|me.proton.core:payment-domain|
|me.proton.core:payment-presentation|
|me.proton.core:plan|
|me.proton.core:plan-data|
|me.proton.core:plan-domain|
|me.proton.core:plan-presentation|
|me.proton.core:presentation|
|me.proton.core:presentation-compose|
|me.proton.core:report|
|me.proton.core:report-dagger|
|me.proton.core:report-data|
|me.proton.core:report-domain|
|me.proton.core:report-presentation|
|me.proton.core:test-android|
|me.proton.core:test-android-instrumented|
|me.proton.core:test-kotlin|
|me.proton.core:user|
|me.proton.core:user-data|
|me.proton.core:user-domain|
|me.proton.core:user-settings|
|me.proton.core:user-settings-data|
|me.proton.core:user-settings-domain|
|me.proton.core:user-settings-presentation|
|me.proton.core:util-android-shared-preferences|
|me.proton.core:util-android-work-manager|
|me.proton.core:util-kotlin|
