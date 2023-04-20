This project contains several gradle plugins. These plugins are used by the core project or can be used by other project as some are published on MavenCentral.
# Setup
Kotlin Gradle DSL using detekt plugin:
```kotlin
settings.gradle.kts

pluginManagement {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") // for snapshot release
        }
        mavenCentral() // for stable release
    }
}
```
```kotlin
build.gradle.kts

plugins {
    id("me.proton.core.gradle-plugins.detekt") version "plugin-version"
}
```
More info about setup, like groovy variant or legacy setup can be found at https://docs.gradle.org/current/userguide/plugins.html.
# Plugins
## Core plugins

- Plugins can be applied via extension functions (inside the `plugins`
  block): `protonAndroidLibrary`, `protonAndroidUiLibrary`, `protonComposeUiLibrary`, `protonDagger`
  , `protonKotlinLibrary`
- Not published on MavenCentral.

Use internally in core project to orchestrate dependencies and apply android/kotlin/dagger convention config.

## Coverage plugin
- Plugin id: `me.proton.core.gradle-plugins.coverage`
- Published on MavenCentral.

Apply the plugin to each module for which the code coverage is needed.
Additional settings can be configured via `protonCoverage` extension,
e.g. setting custom minimum coverage levels, excluding additional files.

## Detekt plugin
- Plugin id: `me.proton.core.gradle-plugins.detekt`
- Published on MavenCentral.

This plugin should be applied to the root `build.gradle` file. It adds a `multiModuleDetekt` task which generate GitLab CI compatible report.

You can setup the following stage in your `.gitlab-ci`:
```yaml
detekt analysis:
  script:
    - ./gradlew multiModuleDetekt
  artifacts:
    reports:
      codequality: config/detekt/reports/mergedReport.json
 ```

## Jacoco plugin
- Plugin id: `me.proton.core.gradle-plugins.jacoco`
- Published on MavenCentral.

This plugin should be applied to the root `build.gradle` file. It adds a `coberturaCoverageReport` task which generate GitLab CI compatible report.
```yaml
coverage report:
  script:
    - ./gradlew -Pci --console=plain coberturaCoverageReport # This also runs allTest
  coverage: /Total.*?(\d{1,3}\.\d{0,2})%/
  artifacts:
    expire_in: 1 week
    paths:
      - ./build/reports/*
    reports:
      cobertura:
        - ./build/reports/cobertura-coverage.xml
```

It also allows for further customization. See the [plugin's README](./jacoco/README.md) for more info.

## Include Core Build plugin
- Plugin id: `me.proton.core.gradle-plugins.include-core-build`
- Published on MavenCentral.

Gradle Settings Plugin to automatically checkout the Proton Core Build, if needed.

```kotlin
settings.gradle.kts

plugins {
  id("me.proton.core.gradle-plugins.include-core-build") version "plugin-version"
}
```
Git Repo Uri:
- isCI -> "https://$username:$token@$host/proton/mobile/android/proton-libs.git"
- else -> "https://github.com/ProtonMail/protoncore_android.git"

Git Clone/Checkout/IncludeBuild:
- if env CORE_COMMIT_SHA exist -> clone/checkout and include full build from provided commit sha, in parent directory.
- if config has includes (see below) -> clone/checkout and include only included projects from provided branch, tag or commit, in parent directory.

Configuration:
```kotlin
settings.gradle.kts

plugins {
  id("me.proton.core.gradle-plugins.include-core-build") version "plugin-version"
}

includeCoreBuild {
  // refreshIntervalMillis.set("86400000") // How often the repository should be updated, default 24h.
  branch.set("main")
  // tag.set("1.0.0")
  // commit.set("commitSha")
  includeBuild("gopenpgp") // Checkout "proton-libs" in parent dir and include "gopenpgp" build.
}
```

Override include with local:
```kotlin
gradle.properties

local.git.proton-libs=../proton-libs
```

**Note: This plugin in based on [IncludeGit Gradle Plugin](https://melix.github.io/includegit-gradle-plugin).**

## Publish-core-libraries plugin
- Plugin id: `publish-core-libraries`
- Not published on MavenCentral.

Use internally in core project to orchestrate [core libraries publication](../README.md#release).

## Tests
- Plugin id: `me.proton.core.gradle-plugins.tests`
- Published on MavenCentral.

This plugin should be applied to the root `build.gradle` file. It adds an `allTest` task which run all unit tests in jvm and Android subprojects.

# Release
Release process is based on [trunk branch for release process](https://trunkbaseddevelopment.com/branch-for-release/).
Release is done by the CI. To trigger a release for version `X.Y.Z`, just push a branch named `release/gradle-plugins/X.Y.Z`.
When the release is successfully done, a tag `release/gradle-plugins/X.Y.Z` is created from the commit used to do the release.

Snapshot releases are also automatically made for every commit on `main` branch. Corresponding version is `main-SNAPSHOT`.

Release implementation is orchestrated by project [publish-core-plugins](./publish-core-plugins).
