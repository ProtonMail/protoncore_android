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

## Setup Detekt
In order to use the all-in-one Detekt configuration, you have to:

1. Setup Plugin
    Kotlin Gradle DSL:
    ```kotlin
    plugins {
        id("me.proton.detekt") version protonDetektVersion
    }
    ```
    Regular Groovy script:
    ```groovy
    buildscript {
      
        dependencies {
            classpath("me.proton:detekt:$protonDetektVersion")
        }
    }
    
    apply(plugin = "me.proton.detekt")
    ```
    
2. Setup the following stage in your `.gitlab-ci`

    ```yaml
    stages:
      - analyze
      # - others ...
    
    #####################
    detekt analysis:
      stage: analyze
      tags:
        - android
      script:
        - ./gradlew multiModuleDetekt
      artifacts:
        reports:
          codequality: config/detekt/reports/mergedReport.json
    ```

    


# Last versions

## Plugins

Detekt: **0.2** - _released on: Nov 26, 2020_

Kotlin: **0.1** - _released on: Oct 09, 2020_

Tests: **0.1** - _released on: Oct 09, 2020_

## Common

### Utils

Util Android Shared Preferences: **0.2.3** - _released on: Dec 18, 2020_

Util Android Work Manager: **0.2.2** - _released on: Dec 18, 2020_

Util Kotlin: **0.2.4** - _released on: Nov 19, 2020_

### Test

Test Kotlin: **0.2** - _released on: Oct 21, 2020_

Test Android: **0.4.2** - _released on: Jan 27, 2021_

Test Android Instrumented: **0.3.2** - _released on: Jan 27, 2021_

## Shared

Domain: **0.2.4** - _released on: Nov 18, 2020_

Presentation: **0.5.4** - _released on: Feb 01, 2021_

Data: **0.2.2** - _released on: Jan 20, 2021_

## Support

### Network

Network: **0.4.5** - _released on: Jan 04, 2021_

Network Domain: **0.4.5** - _released on: Jan 04, 2021_

Network Data: **0.4.5** - _released on: Jan 04, 2021_

### Crypto

Crypto: **0.2.2** - _released on: Jan 20, 2021_

Crypto Common: **0.2.2** - _released on: Jan 20, 2021_

Crypto Android: **0.2.2** - _released on: Jan 20, 2021_

## Features

### Auth

Auth: **0.4** - _released on: Feb 05, 2021_

Auth Domain: **0.4** - _released on: Feb 05, 2021_

Auth Presentation: **0.4** - _released on: Feb 05, 2021_

Auth Data: **0.4** - _released on: Feb 05, 2021_

### Account

Account: **0.2.2** - _released on: Jan 20, 2021_

Account Domain: **0.2.2** - _released on: Jan 20, 2021_

Account Presentation: **0** - _released on: ND_

Account Data: **0.2.2** - _released on: Jan 20, 2021_


### Account Manager

Account Manager: **0.2.3** - _released on: Feb 05, 2021_

Account Manager Domain: **0.2.3** - _released on: Feb 05, 2021_

Account Manager Presentation: **0.2.3** - _released on: Feb 05, 2021_

Account Manager Data: **0.2.3** - _released on: Feb 05, 2021_

Account Manager Dagger: **0.2.3** - _released on: Feb 05, 2021_

### Contacts

Contacts: **0** - _released on: ND_

Contacts Domain: **0** - _released on: ND_

Contacts Presentation: **0** - _released on: ND_

Contacts Data: **0** - _released on: ND_


### Settings

Settings: **0** - _released on: ND_

Settings Domain: **0** - _released on: ND_

Settings Presentation: **0** - _released on: ND_

Settings Data: **0** - _released on: ND_

### Human Verification

Human Verification: **0.2.5** - _released on: Jan 04, 2021_

Human Verification Domain: **0.2.3** - _released on: Nov 12, 2020_

Human Verification Presentation: **0.2.6** - _released on: Feb 01, 2021_

Human Verification Data: **0.2.3** - _released on: Nov 12, 2020_
