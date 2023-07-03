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

package me.proton.core.observability.data.testing

import me.proton.core.observability.data.entity.ObservabilityEventEntity

internal val testObservabilityEventOldFileName = ObservabilityEventEntity(
    id = 1,
    name = "name1",
    version = 1,
    timestamp = 1,
    data = "{\"type\":\"me.proton.core.observability.domain.metrics.SignupFetchDomainsTotalV1\",\"Labels\":{\"status\":\"connectionError\"},\"Value\":1}"
)

internal val testObservabilityEventNewFileName = ObservabilityEventEntity(
    id = 2,
    name = "name1",
    version = 1,
    timestamp = 1,
    data = "{\"type\":\"me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal\",\"Labels\":{\"status\":\"connectionError\"},\"Value\":1}"
)

internal val testObservabilityEventDifferentFileName = ObservabilityEventEntity(
    id = 3,
    name = "name1",
    version = 1,
    timestamp = 1,
    data = "{\"type\":\"someFileName\",\"Labels\":{\"status\":\"connectionError\"},\"Value\":1}"
)
internal val allTestEvents =
    listOf(testObservabilityEventOldFileName, testObservabilityEventNewFileName, testObservabilityEventDifferentFileName)
