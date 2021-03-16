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

1. Setup Plugin in your **root** gradle file
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

Detekt: **0.2.1** - _released on: Mar 08, 2021_

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

Domain: **1.0** - _released on: Feb 17, 2021_

Presentation: **0.5.7** - _released on: Mar 05, 2021_

Data: **1.0** - _released on: Feb 17, 2021_

## Support

### Network

Network: **1.0** - _released on: Feb 17, 2021_

Network Domain: **1.0** - _released on: Feb 17, 2021_

Network Data: **1.0** - _released on: Feb 17, 2021_

### Crypto

Crypto: **1.0.2** - _released on: Mar 05, 2021_

Crypto Common: **1.0.2** - _released on: Mar 05, 2021_

Crypto Android: **1.0.2** - _released on: Mar 05, 2021_

## Features

### Auth

Auth: **1.0.2** - _released on: Mar 16, 2021_

Auth Domain: **1.0.2** - _released on: Mar 16, 2021_

Auth Presentation: **1.0.2** - _released on: Mar 16, 2021_

Auth Data: **1.0.2** - _released on: Mar 16, 2021_

### Account

Account: **1.0.1** - _released on: Feb 17, 2021_

Account Domain: **1.0.1** - _released on: Feb 17, 2021_

Account Presentation: **0** - _released on: ND_

Account Data: **1.0.1** - _released on: Feb 17, 2021_


### Account Manager

Account Manager: **1.0.2** - _released on: Mar 05, 2021_

Account Manager Domain: **1.0.2** - _released on: Mar 05, 2021_

Account Manager Presentation: **1.0.2** - _released on: Mar 05, 2021_

Account Manager Data: **1.0.2** - _released on: Mar 05, 2021_

Account Manager Dagger: **1.0.2** - _released on: Mar 05, 2021_

### Key

Key: **1.0.3** - _released on: Mar 11, 2021_

Key Domain: **1.0.3** - _released on: Mar 11, 2021_

Key Data: **1.0.3** - _released on: Mar 11, 2021_

### User

User: **1.0.2** - _released on: Mar 16, 2021_

User Domain: **1.0.2** - _released on: Mar 16, 2021_

User Data: **1.0.2** - _released on: Mar 16, 2021_

### Contact

Contact: **0.1.2** - _released on: Mar 10, 2021_

Contact Domain: **0.1.2** - _released on: Mar 10, 2021_

Contact Data: **0.1.2** - _released on: Mar 10, 2021_

### Mail Message

Mail Message: **0.1.1** - _released on: Mar 04, 2021_

Mail Message Domain: **0.1.1** - _released on: Mar 04, 2021_

Mail Message Data: **0.1.1** - _released on: Mar 04, 2021_

### Settings

Settings: **0** - _released on: ND_

Settings Domain: **0** - _released on: ND_

Settings Presentation: **0** - _released on: ND_

Settings Data: **0** - _released on: ND_

### Human Verification

Human Verification: **0.2.6** - _released on: Mar 14, 2021_

Human Verification Domain: **0.2.6** - _released on: Mar 14, 2020_

Human Verification Presentation: **0.2.6** - _released on: Feb 01, 2021_

Human Verification Data: **0.2.6** - _released on: Mar 14, 2020_
