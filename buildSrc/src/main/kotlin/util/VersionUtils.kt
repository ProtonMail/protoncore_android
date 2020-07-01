package util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import studio.forface.easygradle.dsl.*

var Project.libVersion
    get() = if (hasProperty("libVersion")) extra["libVersion"] as? Version else null
    set(value) { extra["libVersion"] = value }
