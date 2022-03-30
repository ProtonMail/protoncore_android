/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.network.data.client

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientVersionValidatorTests {

    private val validator = ClientVersionValidatorImpl()

    @Test
    fun `Null version will fail validation`() {
        val version: String? = null
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Empty version will fail validation`() {
        val version = ""
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Only version number info will fail validation`() {
        val version = "1.2.3"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with invalid characters in will fail validation`() {
        val version = "ANDROID-Mail@1.2.3" // Should be lowercase
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with invalid separators will fail validation`() {
        val version = "android_mail_1.2.3"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with valid section info will be validated`() {
        val version = "android-mail-somesection_info@1.2.3"
        assertTrue { validator.validate(version) }
    }

    @Test
    fun `Version code with no section info will be validated`() {
        val version = "android-mail@1.2.3"
        assertTrue { validator.validate(version) }
    }

    @Test
    fun `Version code with invalid characters in section info will fail validation`() {
        val version = "android-mail-somesection.info@1.2.3"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with empty section info but separator will fail validation`() {
        val version = "android-mail-@1.2.3"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with empty version metadata info but separator will fail validation`() {
        val version = "android-mail@1.2.3+"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with empty version identifier info but separator will fail validation`() {
        val version = "android-mail@1.2.3-"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with invalid characters in version metadata will fail validation`() {
        val version = "android-mail@1.2.3+some.metadata"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with versioned dev identifier version will fail validation`() {
        val version = "android-mail@1.2.3-dev.1"
        assertFalse { validator.validate(version) }
    }

    @Test
    fun `Version code with only dev identifier version will pass validation`() {
        val version = "android-mail@1.2.3-dev"
        assertTrue { validator.validate(version) }
    }

    @Test
    fun `Version code with versioned stable, RC, beta or alpha identifiers will pass validation`() {
        val versionWithStableVersion = "android-mail@1.2.3-stable.1"
        val versionWithAlphaVersion = "android-mail@1.2.3-alpha.1"
        val versionWithRCVersion = "android-mail@1.2.3-RC.1"
        val versionWithBetaVersion = "android-mail@1.2.3-beta.1"
        assertTrue { validator.validate(versionWithStableVersion) }
        assertTrue { validator.validate(versionWithAlphaVersion) }
        assertTrue { validator.validate(versionWithRCVersion) }
        assertTrue { validator.validate(versionWithBetaVersion) }
    }

    @Test
    fun `Version code with both version identifier and metadata will pass validation`() {
        val version = "android-mail@1.2.3-stable.1+some-metadata"
        assertTrue { validator.validate(version) }
    }

    @Test
    fun `Version code accepts 4 numeric values`() {
        val version = "android-mail@1.2.3.4"
        assertTrue { validator.validate(version) }
    }

}
