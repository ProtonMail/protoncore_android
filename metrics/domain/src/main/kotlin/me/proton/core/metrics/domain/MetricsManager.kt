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

package me.proton.core.metrics.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.domain.entity.Metrics

public interface MetricsManager {

    /**
     * Send the collected metrics report to the Backend.
     * The caller can optionally provide a [userId] if relevant
     * to the action reported.
     */
    public fun send(userId: UserId?, metrics: Metrics)

}
