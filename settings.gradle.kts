rootProject.name = "Proton Core"

// Common
include(
    // Utils
    // ":lint",
    ":util:kotlin",
    ":util:android:shared-preferences",
    ":util:android:work-manager",
    ":util:gradle",
    // Test
    ":test:kotlin",
    ":test:android",
    ":test:android:instrumented"
)

// Shared
include(
    ":domain",
    ":presentation"
    // ":data"
)

// Support
include(
    // Network
    ":network",
    ":network:domain",
    ":network:data"
)

// Features
include(
    // Contacts
    // ":contacts",
    // ":contacts:domain",
    // ":contacts:presentation",
    // ":contacts:data",
    // Settings
    // ":settings",
    // ":settings:domain",
    // ":settings:presentation",
    // ":settings:data",
    // Human Verification
    ":human-verification",
    ":human-verification:domain",
    ":human-verification:presentation",
    ":human-verification:data"
)

// Demo app
include(
    ":coreexample"
)

// * * * * * * *
// NAMING BELOW
// * * * * * *

// Common
project(":util:kotlin").name = "util-kotlin"
project(":util:android").name = "util-android"
project(":util:android:shared-preferences").name = "util-android-shared-preferences"
project(":util:android:work-manager").name = "util-android-work-manager"
project(":util:gradle").name = "util-gradle"
project(":test:kotlin").name = "test-kotlin"
project(":test:android").name = "test-android"
project(":test:android:instrumented").name = "test-android-instrumented"

// Network
project(":network").name = "network"
project(":network:domain").name = "network-domain"
project(":network:data").name = "network-data"

// Contacts
// ":contacts",
// ":contacts:domain",
// ":contacts:presentation",
// ":contacts:data",

// Settings
// ":settings",
// ":settings:domain",
// ":settings:presentation",
// ":settings:data",

// Human Verification
project(":human-verification").name = "human-verification"
project(":human-verification:domain").name = "human-verification-domain"
project(":human-verification:presentation").name = "human-verification-presentation"
project(":human-verification:data").name = "human-verification-data"
