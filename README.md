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

Detekt: **0.4** - _released on: Sep 13, 2021_

Kotlin: **0.1** - _released on: Oct 09, 2020_

Tests: **0.1** - _released on: Oct 09, 2020_

## Common

### Utils

Util Android Shared Preferences: **1.15** - _released on: Sep 13, 2021_

Util Android Work Manager: **1.15** - _released on: Sep 13, 2021_

Util Kotlin: **1.15.2** - _released on: Sep 23, 2021_

### Test

Test Kotlin: **1.15** - _released on: Sep 13, 2021_

Test Android: **1.15** - _released on: Sep 13, 2021_

Test Android Instrumented: **1.15.1** - _released on: Sep 15, 2021_

## Shared

Domain: **1.15** - _released on: Sep 13, 2021_

Presentation: **1.15.4** - _released on: Oct 01, 2021_

Data: **1.15** - _released on: Sep 13, 2021_

Data Room: **1.16** - _released on: Oct 04, 2021_

## Support

### Network

Network: **1.15.4** - _released on: Oct 05, 2021_

Network Domain: **1.15.4** - _released on: Oct 05, 2021_

Network Data: **1.15.4** - _released on: Oct 05, 2021_

### Crypto

Crypto: **1.15.2** - _released on: Sep 23, 2021_

Crypto Common: **1.15.2** - _released on: Sep 23, 2021_

Crypto Android: **1.15.2** - _released on: Sep 23, 2021_

## Features

### Auth

Auth: **1.15.3** - _released on: Sep 28, 2021_

Auth Domain: **1.15.2** - _released on: Sep 23, 2021_

Auth Presentation: **1.15.3** - _released on: Sep 28, 2021_

Auth Data: **1.15.2** - _released on: Sep 23, 2021_

### Account

Account: **1.15.2** - _released on: Sep 23, 2021_

Account Domain: **1.15.2** - _released on: Sep 23, 2021_

Account Presentation: **0** - _released on: ND_

Account Data: **1.15.2** - _released on: Sep 23, 2021_


### Account Manager

Account Manager: **1.15** - _released on: Sep 13, 2021_

Account Manager Domain: **1.15** - _released on: Sep 13, 2021_

Account Manager Presentation: **1.15** - _released on: Sep 13, 2021_

Account Manager Data: **1.15** - _released on: Sep 13, 2021_

Account Manager Data Db: **1.15** - _released on: Sep 13, 2021_

Account Manager Dagger: **1.15** - _released on: Sep 13, 2021_

### Key

Key: **1.15.3** - _released on: Oct 04, 2021_

Key Domain: **1.15.3** - _released on: Oct 04, 2021_

Key Data: **1.15.3** - _released on: Oct 04, 2021_

### User

User: **1.15.2** - _released on: Sep 23, 2021_

User Domain: **1.15.2** - _released on: Sep 23, 2021_

User Data: **1.15.2** - _released on: Sep 23, 2021_

### Contact

Contact: **1.16-build-10103** - _released on: Oct 04, 2021_

Contact Domain: **1.16-build-10103** - _released on: Oct 04, 2021_

Contact Data: **1.16-build-10103** - _released on: Oct 04, 2021_

### Mail Message

Mail Message: **1.15** - _released on: Sep 13, 2021_

Mail Message Domain: **1.15** - _released on: Sep 13, 2021_

Mail Message Data: **1.15** - _released on: Sep 13, 2021_

### Mail Settings

Mail Settings: **1.15** - _released on: Sep 13, 2021_

Mail Settings Domain: **1.15** - _released on: Sep 13, 2021_

Mail Settings Data: **1.15** - _released on: Sep 13, 2021_

### Human Verification

Human Verification: **1.15.2** - _released on: Sep 23, 2021_

Human Verification Domain: **1.15.2** - _released on: Sep 23, 2021_

Human Verification Presentation: **1.15.2** - _released on: Sep 23, 2021_

Human Verification Data: **1.15.2** - _released on: Sep 23, 2021_

### Countries

Country: **1.15** - _released on: Sep 13, 2021_

Country Domain: **1.15** - _released on: Sep 13, 2021_

Country Data: **1.15** - _released on: Sep 13, 2021_

Country Presentation: **1.15** - _released on: Sep 13, 2021_

### Payment

Payment: **1.15** - _released on: Sep 13, 2021_

Payment Data: **1.15** - _released on: Sep 13, 2021_

Payment Domain: **1.15** - _released on: Sep 13, 2021_

Payment Presentation: **1.15** - _released on: Sep 13, 2021_

### Plan

Plan: **1.15** - _released on: Sep 13, 2021_

Plan Data: **1.15** - _released on: Sep 13, 2021_

Plan Domain: **1.15** - _released on: Sep 13, 2021_

Plan Presentation: **1.15** - _released on: Sep 13, 2021_

### User Settings

User Settings: **1.15.2** - _released on: Sep 23, 2021_

User Settings Data: **1.15.2** - _released on: Sep 23, 2021_

User Settings Domain: **1.15.2** - _released on: Sep 23, 2021_

User Settings Presentation: **1.15.2** - _released on: Sep 23, 2021_
