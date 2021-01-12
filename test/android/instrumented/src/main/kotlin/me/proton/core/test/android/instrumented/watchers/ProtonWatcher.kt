/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package me.proton.core.test.android.instrumented.watchers

/**
 * Mechanism that allows watching for a condition to be met within specified timeout and with specified interval.
 */
class ProtonWatcher {

    private var timeout = DEFAULT_TIMEOUT
    private var watchInterval = DEFAULT_INTERVAL

    interface Condition {
        fun getDescription(): String
        fun checkCondition(): Boolean
    }

    companion object {
        const val CONDITION_NOT_MET = 0
        const val DEFAULT_TIMEOUT = 10_000L
        const val DEFAULT_INTERVAL = 250L
        const val TIMEOUT = 2
        var status = CONDITION_NOT_MET
        private const val CONDITION_MET = 1
        private val instance = ProtonWatcher()

        fun waitForCondition(condition: Condition) {
            // Reset to initial state each time new wait is triggered.
            status = 0
            var timeInterval = 0L
            while (status != CONDITION_MET) {
                if (condition.checkCondition()) {
                    status = CONDITION_MET
                    break
                } else {
                    if (timeInterval < instance.timeout) {
                        timeInterval += instance.watchInterval
                        Thread.sleep(instance.watchInterval)
                    } else {
                        status = TIMEOUT
                    }
                }
            }
        }

        fun setTimeout(ms: Long) {
            instance.timeout = ms
        }

        fun setInterval(ms: Long) {
            instance.watchInterval = ms
        }
    }
}
