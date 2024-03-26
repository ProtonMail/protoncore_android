# Changelog
All notable changes to this project's plugins will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.3.1]

### Changes

- Added product flavor dimension support to environment config plugin.

## [1.3.0]

### Changes

- Added Environment Configuration Plugins:
    - Plugin id: `me.proton.core.gradle-plugins.environment-config`
    - Published on MavenCentral.
    - This plugin should be applied to either build flavor or application build type android extension in `build.gradle.kts` file.

## [1.2.0]

### Changes

- Upgraded to Gradle 8.0.2.
- Added Coverage Plugins:
    - Plugin id: `me.proton.core.gradle-plugins.coverage`
      Apply the plugin to each (non-root) project for which the code coverage is needed.
      Additional settings can be configured via `protonCoverage` extension,
      e.g. setting custom minimum coverage levels, excluding additional files.

    - Plugin id: `me.proton.core.gradle-plugins.global-coverage`
      The plugin is intended to be applied to a separate project, that's only used to
      generate a global coverage report or perform coverage percentage verification.

    - Plugin id: `me.proton.core.gradle-plugins.coverage-config`
      Apply the plugin on the root project.
      Use the `protonCoverage` extension to configure common coverage settings.
      Those settings will be picked up the all the submodules that
      use the Coverage plugin (`me.proton.core.gradle-plugins.coverage`).

- Use detekt's `ignoreAnnotated` config to ignore some warnings for Composable functions

## [1.0.0-alpha03]

### Fixed

- GitLab code quality reports not working because of 'severity' field missing

