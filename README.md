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

Util Android Shared Preferences: **0.2.1** - _released on: Nov 12, 2020_

Util Android Work Manager: **0.2.1** - _released on: Nov 12, 2020_

Util Kotlin: **0.2.4** - _released on: Nov 19, 2020_

### Test

Test Kotlin: **0.2** - _released on: Oct 21, 2020_

Test Android: **0.3.1** - _released on: Nov 12, 2020_

Test Android Instrumented: **0.2** - _released on: Oct 21, 2020_

## Shared

Domain: **0.2.4** - _released on: Nov 18, 2020_

Presentation: **0.5.2** - _released on: Dec 02, 2020_

Data: **0.1.2** - _released on: Nov 12, 2020_

## Support

### Network

Network: **0.4** - _released on: Dec 01, 2020_

Network Domain: **0.3.4** - _released on: Nov 24, 2020_

Network Data: **0.4** - _released on: Dec 01, 2020_

### Crypto

Crypto: **0** - _released on: ND_

Crypto Domain: **0** - _released on: ND_

Crypto Data: **0** - _released on: ND_

## Features

### Auth

Auth: **0.2.4** - _released on: Nov 25, 2020_

Auth Domain: **0.2.4** - _released on: Nov 25, 2020_

Auth Presentation: **0.2.4** - _released on: Nov 25, 2020_

Auth Data: **0.2.4** - _released on: Nov 25, 2020_

### Account

Account: **0.1.3** - _released on: Nov 18, 2020_

Account Domain: **0.1.3** - _released on: Nov 18, 2020_

Account Presentation: **0** - _released on: ND_

Account Data: **0.1.3** - _released on: Nov 18, 2020_


### Account Manager

Account Manager: **0.1.4** - _released on: Nov 18, 2020_

Account Manager Domain: **0.1.4** - _released on: Nov 18, 2020_

Account Manager Presentation: **0.1.4** - _released on: Nov 18, 2020_

Account Manager Data: **0.1.4** - _released on: Nov 18, 2020_

Account Manager Dagger: **0.1.2** - _released on: Nov 12, 2020_

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

Human Verification: **0.2.4** - _released on: Nov 18, 2020_

Human Verification Domain: **0.2.3** - _released on: Nov 12, 2020_

Human Verification Presentation: **0.2.4** - _released on: Nov 18, 2020_

Human Verification Data: **0.2.3** - _released on: Nov 12, 2020_
