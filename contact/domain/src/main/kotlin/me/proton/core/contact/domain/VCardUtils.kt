/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.contact.domain

import ezvcard.VCard
import ezvcard.property.RawProperty
import me.proton.core.util.kotlin.equalsNoCase

/**
 * Helper method to lookup VCard Extended Property [name] inside a given [group]
 */
fun VCard.getProperty(group: String, name: String): RawProperty? =
    this.extendedProperties.firstOrNull {
        it.group.equalsNoCase(group) && it.propertyName.equalsNoCase(name)
    }

/**
 * @return VCard group assigned to the [email] (this is not a "Contact Group"!)
 */
fun VCard.getGroupForEmail(email: String): String? =
    this.emails.firstOrNull {
        email.equalsNoCase(it.value)
    }?.group

fun VCard.getKeysForGroup(group: String): List<ByteArray> =
    this.keys.filter {
        group.equalsNoCase(it.group)
    }.sortedBy {
        it.pref
    }.mapNotNull {
        it.data
    }