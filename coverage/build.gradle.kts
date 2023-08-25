plugins {
    id("me.proton.core.gradle-plugins.global-coverage")
}

publishOption.shouldBePublishedAsLib = false

// Global minimum coverage percentage.
protonCoverage {
    minBranchCoveragePercentage.set(27)
    minLineCoveragePercentage.set(48)
}
