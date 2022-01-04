# Proton Coverage Plugin

This module allows the configuration of code coverage for any multi-module project as well as integration of its coverage results with Gitlab CI or any other CI that uses Cobertura reports.

## How to use

Just add to you root `build.gradle` file:

```kotlin
plugins {
    // ... Some other plugins
    id("me.proton.core.gradle-plugins.jacoco")
}
```

This will automatically apply the plugin to all compatible sub-modules.

## Configuration

The coverage plugin allows for configuration both at the project's level or per-module.

### From root project module

This will be applied to all compatible submodules. You can change the default configuration with this extension on you root `build.gradle` file, where the plugin is applied:

```kotlin
protonCoverageMultiModuleOptions {
    /** Whether we should run test tasks before generating the coverage results or not. Defaults to true. */
    runTestTasksBefore = true

    /** Generate the merged XML report based on the current project's setup. */
    generatesMergedXmlReport = { someConfiguration == "someValue" /* Or simply true / false */ }

    /** Generate the merged HTML report based on the current project's setup. */
    generatesMergedHtmlReport = { true } // Generates merged HTML report (default behavior)

    /** Path to place the merged XML report based on the current project's setup. Should include file and extension. */
    mergedXmlReportPath = { file("someSubFolder/jacoco/coverage.xml") } // Or set to null to use default

    /** Path to place the merged HTML report based on the current project's setup. Should be a directory. */
    mergedHtmlReportPath = { file("someSubFolder") } // Or set to null to use default

    /** Path to place the Cobertura report based on the current project's setup. Should include file and extension. */
    coberturaReportPath = { file("someSubFolder/cobertura/coverage.xml") } // Or set to null to use default

    /** Generate XML reports for each submodule based on that submodule's setup. */
    generatesSubModuleXmlReports = { true }

    /** Generate HTML reports for each submodule based on that submodule's setup. */
    generatesSubModuleHtmlReports = { false }

    /** Patterns to match files to be excluded from every submodule. */
    sharedExcludes = listOf("**/BuildConfig.*", "**/*Generated*.*")
}
```

### For each module

Similar to the root module configuration, we can do some customization of the plugin for each module, just add this extension to the module's `build.gradle` file:

```kotlin
protonCoverageOptions {
    /** Can be used to manually disable coverage for this module. */
    isEnabled = true

    /** Customize which task should run to generate test reports. If null the default implementation will be used. */
    dependsOnTask = "someCustomTestTask"

    /** Customize excluded files for coverage in this module. */
    excludes = listOf("**/BuildConfig.*", "**/*Generated*.*")

    /** Customize the source dirs for this module. */
    sourceDirs = listOf("src/main/java", "src/beta/kotlin")

    /** Generate a XML report for this module or not based on the module's setup. 
     * Takes precedence over root project config, set to null (default value) to use that instead. */
    generatesXmlReport = { someProperty == "someValue" }

    /** Generate a HTML report for this module or not based on the module's setup. 
     * Takes precedence over root project config, set to null (default value) to use that instead. */
    generatesHtmlReport = null
}
```

Also, please note that you *shouldn't use the default Jacoco extensions to setup coverage, as these will be overridden by this plugin*.

## Usage

The coverage plugin registers several tasks:

* A `jacocoTestReport` task for each module. Will also run tests if `runTestTasksBefore` is true.
* A `jacocoMergeReport` task for the whole project. As above, it will run tests if `runTestTasksBefore` is true. Then, it will merge all reports into a single one.
* A `coverageReport` task for the whole project. Will run after `jacocoMergeReport` and log the current coverage percentage into the gradle output. This is used by Gitlab to set the coverage percentage for the branch.
* A `coberturaCoverageReport` task for the whole project. Will run after `coverageReport`. It will translate the merged Jacoco report into a Cobertura one that Gitlab can understand.

You can use any of these tasks as you see fit. The default behavior is to just run `./gradlew coberturaCoverageReport` so the whole testing, coverage parsing and Cobertura conversion is done, but your setup may vary.
